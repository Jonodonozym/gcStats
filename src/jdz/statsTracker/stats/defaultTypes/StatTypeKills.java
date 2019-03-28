
package jdz.statsTracker.stats.defaultTypes;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import jdz.bukkitUtils.components.CombatTimer;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.stats.abstractTypes.BufferedStatType;
import lombok.Getter;

public class StatTypeKills extends BufferedStatType {
	@Getter private static final StatTypeKills instance = new StatTypeKills();
	private static final CombatTimer timer = new CombatTimer(GCStats.getInstance(), 100);

	@Override
	public String getName() {
		return "Kills";
	}

	@Override
	public String valueToString(double value) {
		return ((int) value) + "";
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player killer = event.getEntity().getKiller();

		if (killer == null)
			return;

		add(killer, 1);
	}

	@EventHandler
	public void onCombatLog(PlayerQuitEvent event) {
		if (!timer.isInCombat(event.getPlayer()))
			return;

		add(timer.getLastAttacker(event.getPlayer()), 1);
		StatTypeDeaths.getInstance().add(event.getPlayer(), 1);
	}
}
