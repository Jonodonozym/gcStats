
package jdz.statsTracker.stats;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import jdz.statsTracker.event.StatChangeEvent;
import jdz.statsTracker.database.StatsDatabase;

public abstract class BufferedStatType extends AbstractStatType implements Listener {
	private final Map<Player, Double> onlinePlayerStats = new HashMap<Player, Double>();

	@Override
	public void addPlayer(Player player, double value) {
		onlinePlayerStats.put(player, value);
	}

	@Override
	public double removePlayer(Player player) {
		double value = get(player);
		onlinePlayerStats.remove(player);
		return value;
	}

	@Override
	public double get(Player player) {
		if (!onlinePlayerStats.containsKey(player))
			return 0;
		return onlinePlayerStats.get(player);
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
		double oldValue = onlinePlayerStats.containsKey(player)?onlinePlayerStats.get(player):value;
		if (oldValue != value) {
			StatChangeEvent event = new StatChangeEvent(player, this, oldValue, value);
			event.call();
			if (!event.isCancelled())
				onlinePlayerStats.put(player, value);
		}
	}
}
