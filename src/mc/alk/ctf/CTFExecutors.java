package mc.alk.ctf;

import mc.alk.arena.BattleArena;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.util.MessageUtil;

import org.bukkit.command.CommandSender;

public class CTFExecutors {

	public static boolean addFlag(ArenaPlayer sender, Arena arena, Integer index) {
		if (!(arena instanceof CTFArena)){
			return sendMessage(sender,"&eArena " + arena.getName() +" is not a CTF arena!");
		}
		if (index < 1 || index > 100){
			return sendMessage(sender,"&2index must be between [1-100]!");}

		CTFArena ctf = (CTFArena) arena;
		ctf.addFlag(index -1, sender.getLocation());
		BattleArena.saveArenas(CTF.getSelf());
		return sendMessage(sender,"&2Team &6"+index+"&2 flag added!");
	}

	public static boolean clearFlags(CommandSender sender, Arena arena) {
		if (!(arena instanceof CTFArena)){
			return sendMessage(sender,"&eArena " + arena.getName() +" is not a CTF arena!");
		}
		CTFArena ctf = (CTFArena) arena;
		ctf.clearFlags();
		return sendMessage(sender,"&2Flags cleared for &6"+arena.getName());
	}

	public static boolean sendMessage(CommandSender player, String msg){
		return MessageUtil.sendMessage(player, msg);
	}
	public static boolean sendMessage(ArenaPlayer player, String msg){
		return MessageUtil.sendMessage(player, msg);
	}
}
