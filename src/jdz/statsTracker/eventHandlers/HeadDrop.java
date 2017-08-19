
package jdz.statsTracker.eventHandlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.main.Config;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.util.SqlApi;
import me.Indyuce.bh.ressource.HeadDropEvent;

public class HeadDrop implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHeadDrop(HeadDropEvent e) {
		if (Config.statEnabled.get(StatType.HEAD_DROPS)){
			SqlApi.addStat(Config.dbConnection, e.getReciever(), StatType.HEAD_DROPS, 1);
			AchievementData.updateAchievements(e.getReciever(), StatType.HEAD_DROPS);
		}
	}

}
