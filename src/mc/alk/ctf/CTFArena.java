package mc.alk.ctf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.PlayerStoreController;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.serializers.Persist;
import mc.alk.arena.util.TeamUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class CTFArena extends Arena implements Runnable{
	public static final boolean DEBUG = false;
	
	private static final long FLAG_RESPAWN_TIMER = 20*15L;

	@Persist
	final HashMap<Integer,Location> flagSpawns = new HashMap<Integer,Location>();

	Random rand = new Random();

	final Map<Integer, Flag> flags = new ConcurrentHashMap<Integer, Flag>();

	final ConcurrentHashMap<Team, Flag> teamFlags = new ConcurrentHashMap<Team, Flag>();

	final Map<Team,Integer> scores = new HashMap<Team,Integer>();

	final int capturesToWin = 3;

	int runcount = 0;

	Integer timerid;

	Map<Flag, Integer> respawnTimers = new HashMap<Flag,Integer>();


	@Override
	public void onOpen(){
	}

	@Override
	public void onStart(){
		List<Team> teams = getTeams();
		timerid = Bukkit.getScheduler().scheduleSyncRepeatingTask(CTF.getSelf(), this, 20L,20L);

		/// Set all scores to 0
		/// Set up flag locations
		for (int i=0;i<teams.size();i++){
			Team t = teams.get(i);
			/// Create our flag
			Location l = flagSpawns.get(i).clone();
			l.setY(l.getY()+0.5); /// raise it up just a tad
			ItemStack is = TeamUtil.getTeamHead(i);
			Item item = l.getBlock().getWorld().dropItem(l,is);
			Flag f = new Flag(t,is,l);
			f.setEntity(item);
			flags.put(item.getEntityId(), f);
			teamFlags.put(t, f);

			/// set our score
			scores.put(t, 0);
			if (DEBUG) System.out.println("Team t = " + t);
		}
	}

	@Override
	public void onVictory(MatchResult result){
		cancelTimers();
		removeFlags();
	}

	@Override
	public void onCancel(){
		cancelTimers();
		removeFlags();		
	}


	@MatchEventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event){
		if (event.isCancelled())
			return ;
		if (DEBUG) System.out.println("DROPPPING SOMETHING " + flags.containsKey(event.getPlayer().getEntityId()));
		if (!flags.containsKey(event.getPlayer().getEntityId())){
			return;}
		if (DEBUG) System.out.println("DROPPPING SOMETHING ");
		Item item = event.getItemDrop();
		ItemStack is = item.getItemStack();
		Flag flag = flags.get(event.getPlayer().getEntityId());
		if (flag.sameFlag(is)){ /// Player is dropping a flag
			if (DEBUG) System.out.println("DROPPPING FLAG ");
			playerDroppedFlag(flag, item);
		}
	}

	@MatchEventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event){
		final int id = event.getItem().getEntityId();
		if (!flags.containsKey(id)){
			return;}

		final Player p = event.getPlayer();
		final Team t = getTeam(p);
		/// for some reason if I do flags.remove here... event.setCancelled(true) doesnt work!!!! oO
		/// If anyone can explain this... I would be ecstatic, seriously.
		Flag flag = flags.get(id);
		if (flag.team.equals(t)){
			event.setCancelled(true);
			if (!flag.home){  /// Return the flag back to its home location
				flags.remove(id);
				event.getItem().remove();
				Location l = flag.getHomeLocation();
				Item item = l.getBlock().getWorld().dropItem(l,flag.is);
				flag.setEntity(item);
				flag.home = true;			
				flags.put(item.getEntityId(), flag);
				t.sendMessage(p.getDisplayName() +" has returned your flag home");
			}
		} else {
			/// Give the enemy the flag
			playerPickedUpFlag(p,flag);
			for (Team team : getTeams()){
				if (team.equals(t)){
					team.sendMessage(p.getDisplayName() +" has taken the enemy flag!");					
				} else {
					team.sendMessage(p.getDisplayName() +" has taken your flag!");
				}
			}
		}
		//			playerHoldingFlag.put(p.getEntityId(), event.getItem().getItemStack());

	}

	@MatchEventHandler
	public void onItemDespawn(ItemDespawnEvent event){
		if (DEBUG) System.out.println("despawning" + flags.containsKey(event.getEntity().getEntityId()));
		if (flags.containsKey(event.getEntity().getEntityId())){
			if (DEBUG) System.out.println("not despawning flag!!!!!!!!!!!  " + event.getEntity().getEntityId());
			event.setCancelled(true);
		}		
	}

	@MatchEventHandler
	public void onPlayerDeath(PlayerDeathEvent event){
		Flag flag = flags.remove(event.getEntity().getEntityId());
		if (flag == null)
			return;
		/// we have a flag holder, drop the flag
		event.getDrops();
		if (DEBUG) System.out.println("Dropping flag !!!!!!!!!!!  " + flag.is.getDurability() +"   " + flag.is.getData());
		List<ItemStack> items = event.getDrops();
		for (ItemStack is : items){
			if (flag.sameFlag(is)){
				final int amt = is.getAmount();
				if (amt > 1)
					is.setAmount(amt-1);
				else 
					is.setType(Material.AIR);
				break;
			}
		}
		Location l = event.getEntity().getLocation();
		Item item = l.getBlock().getWorld().dropItem(l,flag.is);
		playerDroppedFlag(flag, item);
	}

	@MatchEventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		if (event.isCancelled())
			return;
		/// Check to see if they moved a block, or if they are holding a flag
		if (!(event.getFrom().getBlockX() != event.getTo().getBlockX()
				|| event.getFrom().getBlockY() != event.getTo().getBlockY()
				|| event.getFrom().getBlockZ() != event.getTo().getBlockZ()) 
				|| !flags.containsKey(event.getPlayer().getEntityId())){
			return;}
		if (this.getMatchState() != MatchState.ONSTART)
			return;
		Team t = this.getTeam(event.getPlayer());
		Flag f = teamFlags.get(t);
		//		Location l = f.getHomeLocation();
		//		Location l2 = event.getTo();
		//		if (DEBUG) System.out.println("id = " + event.getPlayer().getEntityId()+"   " +
		//				flags.containsKey(event.getPlayer().getEntityId()) +"   " + event.getPlayer().getEntityId() + "   ::::"+
		//				nearLocation(f.getHomeLocation(),event.getTo()) +""
		//				+ l.getBlockX() +"  " + l2.getBlockX() +"  " + l.getBlockZ() +"  " + l2.getBlockZ() +"  " + l.getBlockY() +" " +  l2.getBlockY());
		if (f.home && nearLocation(f.getHomeLocation(),event.getTo())){
			teamScored(t);
		}
	}

	private void cancelTimers(){
		if (timerid != null){
			Bukkit.getScheduler().cancelTask(timerid);
			timerid = null;
		}
	}
	private void removeFlags() {
		for (Flag f: flags.values()){
			if (f.ent instanceof Player){
				PlayerStoreController.removeItem(BattleArena.toArenaPlayer((Player) f.ent), f.is);
			} else {
				f.ent.remove();
			}
		}
	}

	private void playerPickedUpFlag(Player player, Flag flag) {
		if (DEBUG) System.out.println(flag.ent.getEntityId() + "    new entid = " + player.getEntityId());
		flags.remove(flag.ent.getEntityId());
		flag.setEntity(player);
		flag.home = false;
		flags.put(player.getEntityId(), flag);
		if (DEBUG) System.out.println("ADDDDDING     " + player.getEntityId());
		//		playerHoldingFlag.put(player.getEntityId(), flag);
	}

	private void playerDroppedFlag(Flag flag, Item item) {
		//		playerHoldingFlag.remove(player.getEntityId());
		if (DEBUG) System.out.println(flag.ent.getEntityId() + "    new entid = " + item.getEntityId());
		flags.remove(flag.ent.getEntityId());
		flag.setEntity(item);
		flags.put(item.getEntityId(), flag);
		startFlagRespawnTimer(flag);
	}
	
	private void spawnFlag(Flag flag){
		Entity ent = flag.getEntity();
		flags.remove(ent.getEntityId());
		if (ent != null && ent instanceof Item){
			ent.remove();}
		Location l = flag.getHomeLocation();
		Item item = l.getBlock().getWorld().dropItem(l,flag.is);
		flag.setEntity(item);
		flag.home = true;		
		flags.put(item.getEntityId(), flag);
	}


	private void startFlagRespawnTimer(final Flag flag) {
		cancelFlagRespawnTimer(flag);
		Integer timerid = Bukkit.getScheduler().scheduleSyncDelayedTask(CTF.getSelf(), new Runnable(){
			@Override
			public void run() {
				spawnFlag(flag);
				Team team = flag.getTeam();
				team.sendMessage("Your flag has been returned home");
			}
		}, FLAG_RESPAWN_TIMER);
		respawnTimers.put(flag, timerid);
	}

	private void cancelFlagRespawnTimer(Flag flag){
		Integer timerid = respawnTimers.get(flag);
		if (timerid != null)
			Bukkit.getScheduler().cancelTask(timerid);		
	}

	private void teamScored(Team team) {
		int teamScore = addScore(team);
		if (DEBUG) System.out.println("teamScore = " + teamScore +"   " + capturesToWin);
		StringBuilder sb = new StringBuilder();
		List<Team> teams = getTeams();
		boolean first = true;
		for (int i=0;i<teams.size();i++){
			Team t = teams.get(i);
			if (!first) sb.append(ChatColor.YELLOW + ", ");
			sb.append(t.getDisplayName() +"&6:" + scores.get(t));
			first = false;
		}
		this.getMatch().sendMessage(team.getDisplayName() +" &ehas captured the flag!");
		this.getMatch().sendMessage("&eScore " + sb.toString() +"  &eScoreToWin=&6"+capturesToWin);
		if (teamScore >= capturesToWin ){
			setWinner(team);}
		resetFlags();
	}


	private void resetFlags() {
		/// remove flags from field
		removeFlags();
		for (Flag f: flags.values()){
			spawnFlag(f);
		}
	}

	public static boolean nearLocation(final Location l1, final Location l2){
		return l1.getWorld().getUID().equals(l2.getWorld().getUID()) && Math.abs(l1.getX() - l2.getX()) < 1.5
				&& Math.abs(l1.getZ() - l2.getZ()) < 1.5 && Math.abs(l1.getBlockY() - l2.getBlockY()) < 2;
	}

	public Map<Integer, Location> getFlagLocaitons() {
		return flagSpawns;
	}

	public void addFlag(Integer i, Location location) {
		flagSpawns.put(i, location);
	}

	public void clearFlags() {
		flagSpawns.clear();
	}

	private Integer addScore(Team team) {
		Integer i = scores.get(team);
		scores.put(team, ++i);
		return i;
	}

	@Override
	public boolean valid(){
		return super.valid() && flagSpawns.size() >= 2;
	}

	@Override
	public List<String> getInvalidReasons(){
		List<String> reasons = new ArrayList<String>();
		if (flagSpawns == null || flagSpawns.size() < 2){
			reasons.add("You need to add at least 2 flags!");}
		reasons.addAll(super.getInvalidReasons());
		return reasons;
	}

	@Override
	public void run() {
		boolean extraeffects = (runcount++ % 2 == 0);
		for (Flag flag: flags.values()){
			Location l = flag.getCurrentLocation();
			l.getWorld().playEffect(l, Effect.MOBSPAWNER_FLAMES, 0);
			if (extraeffects){
				l = l.clone();
				l.setX(l.getX() + rand.nextInt(4)-2);
				l.setZ(l.getZ() + rand.nextInt(4)-2);
				l.setY(l.getY() + rand.nextInt(2)-1);
				l.getWorld().playEffect(l, Effect.MOBSPAWNER_FLAMES, 0);
			}
		}
	}

}
