
package jdz.statsTracker.stats.abstractTypes;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import jdz.statsTracker.GCStats;
import jdz.statsTracker.event.StatChangeEvent;

public abstract class HookedStatType extends AbstractStatType {
	private final Map<Player, Double> lastValues = new HashMap<>();
	private final BukkitTask task;

	protected HookedStatType() {
		this(10);
	}

	protected HookedStatType(int refreshRate) {
		task = Bukkit.getScheduler().runTaskTimer(GCStats.getInstance(), ()->{
			for (Player player : Bukkit.getOnlinePlayers()) {
				double newValue = get(player);
				double oldValue = lastValues.containsKey(player) ? lastValues.get(player) : 0;

				StatChangeEvent event = new StatChangeEvent(player, player.getUniqueId(), this, oldValue, newValue);
				event.call();
				if (!event.isCancelled())
					lastValues.put(player, newValue);
			}
		}, refreshRate, refreshRate);
	}

	@Override
	public boolean hasPlayer(Player player) {
		return lastValues.containsKey(player);
	}
	
	public void disable() {
		task.cancel();
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
