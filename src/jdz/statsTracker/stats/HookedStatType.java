
package jdz.statsTracker.stats;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.misc.TimedTask;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.event.StatChangeEvent;

public abstract class HookedStatType extends AbstractStatType {
	private final Map<Player, Double> lastValues = new HashMap<Player, Double>();
	private final TimedTask task;

	protected HookedStatType() {
		this(10);
	}

	protected HookedStatType(int refreshRate) {
		task = new TimedTask(GCStats.instance, refreshRate, () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				double newValue = get(player);
				double oldValue = lastValues.containsKey(player) ? lastValues.get(player) : 0;

				StatChangeEvent event = new StatChangeEvent(player, this, oldValue, newValue);
				event.call();
				if (!event.isCancelled())
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
}
