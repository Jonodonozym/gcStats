
package jdz.statsTracker.eventHandlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.main.Config;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.util.SqlApi;

public class ExpGain implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onExpGain(PlayerExpChangeEvent e) {
		if (Config.enabledStats.contains(StatType.EXP_GAINED) && e.getAmount() > 0) {
			SqlApi.addStat(Config.dbConnection, e.getPlayer(), StatType.EXP_GAINED, e.getAmount());
			AchievementData.updateAchievements(e.getPlayer(), StatType.EXP_GAINED);
		}
	}
}
