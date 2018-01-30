
package jdz.statsTracker.stats.defaults;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerExpChangeEvent;

import jdz.statsTracker.stats.BufferedStatType;
import lombok.Getter;

class StatTypeExpGained extends BufferedStatType{
	@Getter private static final StatTypeExpGained instance = new StatTypeExpGained();

	@Override
	public String getName() {
		return "Exp gained";
	}

	@Override
	public String valueToString(double value) {
		return ((int)value)+"";
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onExpGain(PlayerExpChangeEvent e) {
		if (e.getAmount() <= 0)
			return;
		
		set(e.getPlayer(), get(e.getPlayer())+e.getAmount());
	}
}
