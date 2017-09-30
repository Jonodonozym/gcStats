
package jdz.statsTracker.eventHandlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import jdz.statsTracker.stats.StatBuffer;
import jdz.statsTracker.stats.StatType;

public class BlockPlace implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent e) {
		StatBuffer.addStat(e.getPlayer(), StatType.BLOCKS_PLACED, 1);
	}
}
