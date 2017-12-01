
package jdz.statsTracker.eventHandlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsDatabase;

public class BlockPlace implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent e) {
		StatsDatabase.getInstance().addStat(e.getPlayer(), StatType.BLOCKS_PLACED, 1);
	}
}
