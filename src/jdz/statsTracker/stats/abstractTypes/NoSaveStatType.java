
package jdz.statsTracker.stats.abstractTypes;

import java.util.UUID;

import org.bukkit.OfflinePlayer;

public abstract class NoSaveStatType extends BufferedStatType {

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
}
