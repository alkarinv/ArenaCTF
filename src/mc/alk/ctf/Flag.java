package mc.alk.ctf;

import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.SerializerUtil;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class Flag {
	static int count = 0;
	final int id = count++; /// our id

	Entity ent; /// What is our flag (item or carried by player)

	boolean home; /// is our flag at home

	final Team team; /// which team this flag belongs to

	final ItemStack is; /// what type of item is our flag

	final Location homeLocation; /// our spawn location

	public Flag(Team team, ItemStack is, Location homeLocation){
		this.team = team;
		this.home = true;
		this.is = is;
		this.homeLocation = homeLocation;
	}

	public void setEntity(Entity entity) {this.ent = entity;}
	public Location getCurrentLocation() {return ent.getLocation();}
	public Location getHomeLocation() {return homeLocation;}

	public boolean sameFlag(ItemStack is2) {
		return is.getType() == is2.getType() && is.getDurability() == is2.getDurability();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof Flag)) return false;
		return this.hashCode() == ((Flag) other).hashCode();
	}

	@Override
	public int hashCode() { return id;}

	public Entity getEntity() {
		return ent;
	}

	public Team getTeam() {
		return team;
	}

	public boolean isHome() {
		return home;
	}

	public void setHome(boolean home) {
		this.home = home;
	}

	@Override
	public String toString(){
		return String.format("[Flag %d: ent=%s, home=%s, team=%d, is=%s, homeloc=%s]",
				id,ent == null ? "null" :ent.getType(),home,
				team == null ? "null" : team.getId(),
				is == null ? "null" : InventoryUtil.getItemString(is),
				homeLocation==null? "null" : SerializerUtil.getLocString(homeLocation));
	}
}
