
package jdz.statsTracker.eventHandlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.main.Config;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.util.SqlApi;

public class BlockPlace implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent e) {
		if (Config.enabledStats.contains(StatType.BLOCKS_PLACED)) {
			SqlApi.addStat(Config.dbConnection, e.getPlayer(), StatType.BLOCKS_PLACED, 1);
			AchievementData.updateAchievements(e.getPlayer(), StatType.BLOCKS_PLACED);
		}
	}
}
