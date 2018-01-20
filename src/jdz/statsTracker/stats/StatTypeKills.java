
package jdz.statsTracker.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import lombok.Getter;

class StatTypeKills extends BufferedStatType {
	@Getter private static final StatTypeKills instance = new StatTypeKills();

	@Override
	public String getName() {
		return "Kills";
	}

	@Override
	public String valueToString(double value) {
		return ((int)value)+"";
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e){
		Player killer = e.getEntity().getKiller();
		
		if (killer == null)
			return;
		
		set(killer, get(killer)+1);
	}
}
