
package jdz.statsTracker.stats.defaults;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

import jdz.statsTracker.stats.BufferedStatType;
import lombok.Getter;

public class StatTypeBlocksPlaced extends BufferedStatType {
	@Getter private static final StatTypeBlocksPlaced instance = new StatTypeBlocksPlaced();

	@Override
	public String getName() {
		return "Blocks placed";
	}

	@Override
	public String valueToString(double value) {
		return ((int) value) + "";
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		set(e.getPlayer(), get(e.getPlayer()) + 1);
	}
}
