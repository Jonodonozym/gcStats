
package jdz.statsTracker.stats.abstractTypes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import jdz.statsTracker.event.StatChangeEvent;
import jdz.statsTracker.stats.StatsDatabase;

public abstract class BufferedStatType extends AbstractStatType implements Listener {
	
	public static final BufferedStatType makeDouble(String name) {
		return new BufferedStatType() {
			
			@Override
			public String valueToString(double value) {
				return value+"";
			}
			
			@Override
			public String getName() {
				return name;
			}
		};
	}
	
	public static final BufferedStatType makeInt(String name) {
		return new BufferedStatType() {
			
			@Override
			public String valueToString(double value) {
				return ((int)value)+"";
			}
			
			@Override
			public String getName() {
				return name;
			}
		};
	}
	
	protected final Map<UUID, Double> onlinePlayerStats = new HashMap<UUID, Double>();
	private final Set<UUID> hasFetched = new HashSet<UUID>();

	@Override
	public void addPlayer(Player player, double value) {
		addEntry(player.getUniqueId(), value);
	}

	@Override
	public boolean hasPlayer(Player player) {
		return onlinePlayerStats.containsKey(player.getUniqueId());
	}

	protected void addEntry(UUID uuid, double value) {
		onlinePlayerStats.put(uuid, value);
		hasFetched.add(uuid);
	}

	@Override
	public double removePlayer(Player player) {
		return removeEntry(player.getUniqueId());
	}

	protected double removeEntry(UUID uuid) {
		hasFetched.remove(uuid);
		return onlinePlayerStats.containsKey(uuid) ? onlinePlayerStats.remove(uuid) : getDefault();
	}

	@Override
	public double get(Player player) {
		return get(player.getUniqueId());
	}

	protected double get(UUID uuid) {
		if (!onlinePlayerStats.containsKey(uuid))
			onlinePlayerStats.put(uuid, getDefault());
		return onlinePlayerStats.get(uuid);
	}

	public void add(Player player, double amount) {
		if (!hasFetched(player))
			return;
		set(player, get(player) + amount);
	}

	public void add(OfflinePlayer player, double amount) {
		if (player.isOnline())
			add(player.getPlayer(), amount);
		else
			StatsDatabase.getInstance().addStat(player, this, amount);
	}

	public void set(Player player, double value) {
		if (!hasFetched(player))
			return;
		set(player.getUniqueId(), value);
	}

	public void set(OfflinePlayer player, double amount) {
		if (player.isOnline())
			set(player.getPlayer(), amount);
		else
			StatsDatabase.getInstance().setStat(player, this, amount);
	}

	public void set(UUID uuid, double value) {
		if (!onlinePlayerStats.containsKey(uuid))
			onlinePlayerStats.put(uuid, getDefault());
		double oldValue = onlinePlayerStats.get(uuid);
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

	public boolean hasFetched(Player player) {
		return hasFetched.contains(player.getUniqueId());
	}

	public void setHasFetched(Player player, boolean hasFetched) {
		if (hasFetched)
			this.hasFetched.add(player.getUniqueId());
		else
			this.hasFetched.remove(player.getUniqueId());
	}
}
