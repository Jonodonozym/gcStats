
package jdz.statsTracker.eventHandlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import jdz.statsTracker.stats.StatBuffer;
import jdz.statsTracker.stats.StatType;

public class BlockBreak implements Listener{

	@EventHandler
	public void onBreak(BlockBreakEvent e){
		StatBuffer.addStat(e.getPlayer(), StatType.BLOCKS_MINED, 1);
	}
}
