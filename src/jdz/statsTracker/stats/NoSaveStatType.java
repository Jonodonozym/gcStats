
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
	public double removePlayer(Player player) {
		return get(player);
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
		for (UUID uuid: onlinePlayerStats.keySet())
			onlinePlayerStats.put(uuid, getDefault());
	}

	@Override
	public void updateDatabase(Player player) { }
}
