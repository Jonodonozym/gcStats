
package jdz.statsTracker.stats.defaults;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

import jdz.statsTracker.stats.BufferedStatType;
import lombok.Getter;

public class StatTypeMobKills extends BufferedStatType {
	@Getter private static final StatTypeMobKills instance = new StatTypeMobKills();

	@Override
	public String getName() {
		return "Monsters killed";
	}

	@Override
	public String valueToString(double value) {
		return ((int) value) + "";
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onMobDeath(EntityDeathEvent event) {
		Player player = event.getEntity().getKiller();
		if (player == null)
			return;

		set(player, get(player) + 1);
	}
}
