
package jdz.statsTracker.eventHandlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.main.Config;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.util.SqlApi;

public class BlockBreak implements Listener {

	@EventHandler(priority=EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent e){
		if (Config.enabledStats.contains(StatType.BLOCKS_MINED)){
			SqlApi.addStat(Config.dbConnection, e.getPlayer(), StatType.BLOCKS_MINED, 1);
			AchievementData.updateAchievements(e.getPlayer(), StatType.BLOCKS_MINED);
		}
	}
}
