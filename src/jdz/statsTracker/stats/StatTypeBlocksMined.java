
package jdz.statsTracker.stats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

import lombok.Getter;

class StatTypeBlocksMined extends BufferedStatType{
	@Getter private static final StatTypeBlocksMined instance = new StatTypeBlocksMined();

	@Override
	public String getName() {
		return "Blocks mined";
	}

	@Override
	public String valueToString(double value) {
		return ((int)value)+"";
	}

	@EventHandler
	public void onBreak(BlockBreakEvent e){
		set(e.getPlayer(), get(e.getPlayer())+1);
	}
}
