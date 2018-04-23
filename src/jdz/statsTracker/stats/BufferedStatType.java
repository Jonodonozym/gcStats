
package jdz.statsTracker.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import jdz.statsTracker.event.StatChangeEvent;
import jdz.statsTracker.database.StatsDatabase;

public abstract class BufferedStatType extends AbstractStatType implements Listener {
	private final Map<UUID, Double> onlinePlayerStats = new HashMap<UUID, Double>();

	@Override
	public void addPlayer(Player player, double value) {
		addEntry(player.getUniqueId(), value);
	}
	
	protected void addEntry(UUID uuid, double value) {
		onlinePlayerStats.put(uuid, value);
	}

	@Override
	public double removePlayer(Player player) {
		return removeEntry(player.getUniqueId());
	}
	
	protected double removeEntry(UUID uuid) {
		double value = get(uuid);
		onlinePlayerStats.remove(uuid);
		return value;
	}

	@Override
	public double get(Player player) {
		return get(player.getUniqueId());
	}
	
	protected double get(UUID uuid) {
		if (!onlinePlayerStats.containsKey(uuid))
			return getDefault();
		return onlinePlayerStats.get(uuid);
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
			StatChangeEvent event = new StatChangeEvent(player, player.getUniqueId(), this, oldValue, value);
			event.call();
			if (!event.isCancelled())
				onlinePlayerStats.put(player.getUniqueId(), value);
		}
	}

	protected final void set(UUID uuid, double value) {
		double oldValue = onlinePlayerStats.containsKey(uuid) ? onlinePlayerStats.get(uuid) : value;
		if (oldValue != value) {
			Player player = Bukkit.getPlayer(uuid);
			StatChangeEvent event = new StatChangeEvent(player, uuid, this, oldValue, value);
			event.call();
			if (!event.isCancelled())
				onlinePlayerStats.put(uuid, value);
		}
	}

	protected final Set<UUID> getAllEntries() {
		return onlinePlayerStats.keySet();
	}
}
