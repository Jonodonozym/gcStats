
package jdz.statsTracker.eventHandlers;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import jdz.statsTracker.GCStatsTrackerConfig;
import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.stats.StatsDatabase;
import jdz.statsTracker.stats.StatType;
import subside.plugins.koth.events.KothEndEvent;
import subside.plugins.koth.gamemodes.RunningKoth.EndReason;

public class KothWin implements Listener{

	@EventHandler(priority=EventPriority.LOWEST)
	public void onKothEvent(KothEndEvent e){
		if (GCStatsTrackerConfig.enabledStats.contains(StatType.KOTH_WINS) && e.getReason().equals(EndReason.WON)){
			Collection<Player> players = e.getWinner().getAvailablePlayers(e.getKoth());
			for (Player p: players){
				StatsDatabase.getInstance().addStat(p, StatType.KOTH_WINS, 1);
				AchievementData.checkAchievements(p, StatType.KOTH_WINS);
			}
		}
	}
}
