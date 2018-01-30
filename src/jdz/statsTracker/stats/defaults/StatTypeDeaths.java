
package jdz.statsTracker.stats.defaults;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import jdz.statsTracker.stats.BufferedStatType;
import lombok.Getter;

class StatTypeDeaths extends BufferedStatType {
	@Getter private static final StatTypeDeaths instance = new StatTypeDeaths();

	@Override
	public String getName() {
		return "Deaths";
	}

	@Override
	public String valueToString(double value) {
		return ((int)value)+"";
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e){
		set(e.getEntity(), get(e.getEntity())+1);
	}
}
