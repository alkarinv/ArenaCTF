package mc.alk.ctf;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.executors.CustomCommandExecutor;
import mc.alk.arena.executors.MCCommand;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.arenas.Arena;

import org.bukkit.command.CommandSender;

public class CTFExecutor extends CustomCommandExecutor{

	@MCCommand(cmds={"addFlag"}, inGame=true, admin=true)
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

	@MCCommand(cmds={"clearFlags"}, admin=true)
	public static boolean clearFlags(CommandSender sender, Arena arena) {
		if (!(arena instanceof CTFArena)){
			return sendMessage(sender,"&eArena " + arena.getName() +" is not a CTF arena!");
		}
		CTFArena ctf = (CTFArena) arena;
		ctf.clearFlags();
		return sendMessage(sender,"&2Flags cleared for &6"+arena.getName());
	}

	@MCCommand(cmds={"capture"}, op=true)
	public static boolean capture(CommandSender sender, ArenaPlayer player) {
		Match m = BattleArena.getBAController().getMatch(player);
		((CTFArena)m.getArena()).captured(player);
        return sendMessage(sender,"&2Flag was captured by &6"+player.getName());
	}
}
