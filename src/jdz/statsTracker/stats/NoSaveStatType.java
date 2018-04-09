
package jdz.statsTracker.stats;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

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

	@Override
	public double get(Player player) {
		if (!onlinePlayerStats.containsKey(player.getUniqueId()))
			onlinePlayerStats.put(player.getUniqueId(), getDefault());
		return onlinePlayerStats.get(player.getUniqueId());
	}

	public void resetAll() {
		for (UUID uuid : onlinePlayerStats.keySet())
			onlinePlayerStats.put(uuid, getDefault());
	}

	@Override
	public void updateDatabase(Player player) {}
}
