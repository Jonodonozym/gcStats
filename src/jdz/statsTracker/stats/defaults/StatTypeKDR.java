
package jdz.statsTracker.stats.defaults;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;

import jdz.statsTracker.stats.BufferedStatType;
import lombok.Getter;

class StatTypeKDR extends BufferedStatType {
	@Getter private static final StatTypeKDR instance = new StatTypeKDR();

	@Override
	public String getName() {
		return "KDR";
	}

	@Override
	public String valueToString(double value) {
		return value+"";
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent e){
		Player killed = e.getEntity();
	
		double kills = StatTypeKills.getInstance().get(killed);
		double deaths = StatTypeDeaths.getInstance().get(killed);
		if (deaths > 0)
			set(killed, Math.round(kills/deaths*100)/100.0);

		Player killer = e.getEntity().getKiller();
		if (killer == null)
			return;

		kills = StatTypeKills.getInstance().get(killed);
		deaths = StatTypeDeaths.getInstance().get(killed);
		if (deaths > 0)
			set(killed, Math.round(kills/deaths*100)/100.0);
		else
			set(killer, kills);
	}
}
