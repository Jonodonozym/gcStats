
package jdz.statsTracker.stats.abstractTypes;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public abstract class NoSaveStatType extends BufferedStatType {

	@Override
	protected void addEntry(UUID uuid, double value) {
		if (!onlinePlayerStats.containsKey(uuid))
			super.addEntry(uuid, value);
	}

	@Override
	protected double removeEntry(UUID uuid) {
		return onlinePlayerStats.get(uuid);
	}

	@Override
	public void add(OfflinePlayer player, double amount) {
		if (player.isOnline())
			add(player.getPlayer(), amount);
	}

	@Override
	public void set(OfflinePlayer player, double amount) {
		if (player.isOnline())
			set(player.getPlayer(), amount);
	}

	@Override
	public double get(OfflinePlayer player) {
		if (player.isOnline())
			return get(player.getPlayer());
		return getDefault();
	}

	public void resetAll() {
		for (UUID uuid : getAllEntries())
			set(uuid, getDefault());
	}
	
	@Override
	public boolean hasFetched(Player player) {
		return true;
	}
	
}
