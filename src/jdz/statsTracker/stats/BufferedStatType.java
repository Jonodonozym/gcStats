
package jdz.statsTracker.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import jdz.statsTracker.event.StatChangeEvent;
import jdz.statsTracker.database.StatsDatabase;

public abstract class BufferedStatType extends AbstractStatType implements Listener {
	protected final Map<UUID, Double> onlinePlayerStats = new HashMap<UUID, Double>();

	@Override
	public void addPlayer(Player player, double value) {
		onlinePlayerStats.put(player.getUniqueId(), value);
	}

	@Override
	public double removePlayer(Player player) {
		double value = get(player);
		onlinePlayerStats.remove(player.getUniqueId());
		return value;
	}

	@Override
	public double get(Player player) {
		if (!onlinePlayerStats.containsKey(player.getUniqueId()))
			return getDefault();
		return onlinePlayerStats.get(player.getUniqueId());
	}

	public void add(OfflinePlayer player, double amount) {
		if (player.isOnline())
			add(player.getPlayer(), amount);
		else
			StatsDatabase.getInstance().addStat(player, this, amount);
	}

	public void set(OfflinePlayer player, double amount) {
		if (player.isOnline())
			set(player.getPlayer(), amount);
		else
			StatsDatabase.getInstance().setStat(player, this, amount);
	}

	public void add(Player player, double amount) {
		set(player, get(player) + amount);
	}

	public void set(Player player, double value) {
		double oldValue = onlinePlayerStats.containsKey(player.getUniqueId())
				? onlinePlayerStats.get(player.getUniqueId())
				: value;
		if (oldValue != value) {
			StatChangeEvent event = new StatChangeEvent(player, this, oldValue, value);
			event.call();
			if (!event.isCancelled())
				onlinePlayerStats.put(player.getUniqueId(), value);
		}
	}
}
