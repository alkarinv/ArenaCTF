package mc.alk.ctf;

import mc.alk.arena.BattleArena;
import mc.alk.arena.executors.CustomCommandExecutor;
import mc.alk.arena.executors.MCCommand;
import mc.alk.arena.objects.arenas.Arena;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CTFExecutor extends CustomCommandExecutor{

	@MCCommand(cmds={"addFlag"}, admin=true)
	public static boolean addFlag(Player sender, Arena arena, Integer index) {
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

	@MCCommand(cmds={"clearFlags"}, admin=true)
	public static boolean clearFlags(CommandSender sender, Arena arena) {
		if (!(arena instanceof CTFArena)){
			return sendMessage(sender,"&eArena " + arena.getName() +" is not a CTF arena!");
		}
		CTFArena ctf = (CTFArena) arena;
		ctf.clearFlags();
		return sendMessage(sender,"&2Flags cleared for &6"+arena.getName());
	}
}
