
package jdz.statsTracker.stats;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.misc.TimedTask;
import jdz.statsTracker.GCStatsTracker;
import jdz.statsTracker.event.StatChangeEvent;

public abstract class HookedStatType implements StatType {
	private final Map<Player, Double> lastValues = new HashMap<Player, Double>();
	private final TimedTask task;

	protected HookedStatType() {
		this(10);
	}

	protected HookedStatType(int refreshRate) {
		task = new TimedTask(GCStatsTracker.instance, refreshRate, () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				double newValue = get(player);
				if (!lastValues.containsKey(player)) {
					lastValues.put(player, newValue);
					return;
				}
				if (newValue != lastValues.get(player))
					new StatChangeEvent(player, this, lastValues.get(player), newValue).call();
				lastValues.put(player, newValue);
			}
		});
		task.start();
	}

	void disable() {
		task.stop();
	}

	@Override
	public void addPlayer(Player player, double value) {
		lastValues.put(player, -1.0);
	}

	@Override
	public double removePlayer(Player player) {
		lastValues.remove(player);
		return get(player);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof StatType))
			return false;
		return ((StatType) other).getID() == getID();
	}

	@Override
	public int hashCode() {
		return getID();
	}
}
