
package jdz.statsTracker.eventHandlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

import jdz.statsTracker.GCStatsTrackerConfig;
import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.stats.StatsDatabase;
import jdz.statsTracker.stats.StatType;

public class ExpGain implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onExpGain(PlayerExpChangeEvent e) {
		if (GCStatsTrackerConfig.enabledStats.contains(StatType.EXP_GAINED) && e.getAmount() > 0) {
			StatsDatabase.getInstance().addStat(e.getPlayer(), StatType.EXP_GAINED, e.getAmount());
			AchievementData.checkAchievements(e.getPlayer(), StatType.EXP_GAINED);
		}
	}
}
