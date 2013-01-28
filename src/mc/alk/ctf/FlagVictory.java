package mc.alk.ctf;

import java.util.List;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.messages.MatchIntervalMessageEvent;
import mc.alk.arena.events.matches.messages.MatchTimeExpiredMessageEvent;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.victoryconditions.PointTracker;
import mc.alk.arena.objects.victoryconditions.VictoryCondition;
import mc.alk.arena.util.TimeUtil;

public class FlagVictory extends VictoryCondition{

	final PointTracker scores;
	final Integer capturesToWin;

	public FlagVictory(Match match, Integer capturesToWin) {
		super(match);
		this.scores = new PointTracker(match);
		this.capturesToWin = capturesToWin;
	}

	public String getScoreString() {
		StringBuilder sb = new StringBuilder(match.getParams().getPrefix()+"&eScore ");
		List<Team> teams = match.getTeams();
		boolean first = true;
		for (int i=0;i<teams.size();i++){
			if (!first) sb.append("&e, ");
			Team t = teams.get(i);
			sb.append(t.getDisplayName() +"&e:&2" + scores.getPoints(t)+"&6/"+capturesToWin);
			first = false;
		}
		return sb.toString();
	}

	public Integer addScore(Team team) {
		return scores.addPoints(team, 1);
	}

	@MatchEventHandler
	public void onMatchIntervalMessage(MatchIntervalMessageEvent event){
		StringBuilder sb = new StringBuilder();
		sb.append("&4<---- &f"+TimeUtil.convertSecondsToString(event.getTimeRemaining()) +" remaining &4---->\n");
		sb.append(getScoreString());
		event.setMatchMessage(sb.toString());
	}

	@MatchEventHandler
	public void onMatchTimeExpiredMessage(MatchTimeExpiredMessageEvent event){
		StringBuilder sb = new StringBuilder();
		switch(match.getResult().getResult()){
		case WIN:
			sb.append(match.getParams().getPrefix()+"&e Congratulations to &6");
			for (Team t: match.getResult().getVictors()){
				sb.append(t.getDisplayName() +" ");
			}
			break;
		case DRAW:
			sb.append(match.getParams().getPrefix()+" The match was drawn between &6");
			for (Team t: match.getResult().getDrawers()){
				sb.append(t.getDisplayName() +" ");
			}
			break;
		default:
			/// not really sure...
		}
		event.setMatchMessage(sb.toString());
	}
}
