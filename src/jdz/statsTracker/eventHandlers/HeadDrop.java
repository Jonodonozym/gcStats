
package jdz.statsTracker.eventHandlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import jdz.statsTracker.GCStatsTrackerConfig;
import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.stats.StatsDatabase;
import jdz.statsTracker.stats.StatType;
import me.Indyuce.bh.ressource.HeadDropEvent;

public class HeadDrop implements Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	public void onHeadDrop(HeadDropEvent e) {
		if (GCStatsTrackerConfig.enabledStats.contains(StatType.HEAD_DROPS)){
			StatsDatabase.getInstance().addStat(e.getReciever(), StatType.HEAD_DROPS, 1);
			AchievementData.checkAchievements(e.getReciever(), StatType.HEAD_DROPS);
		}
	}

}
