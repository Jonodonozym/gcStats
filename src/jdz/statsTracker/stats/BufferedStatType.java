
package jdz.statsTracker.stats;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class BufferedStatType implements StatType, Listener{
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
		return onlinePlayerStats.get(player);
	}
	
	@Override
	public abstract String getName();
	@Override
	public abstract String valueToString(double value);
	
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
		double oldValue = onlinePlayerStats.get(player);
		if (oldValue != value) {
			new StatChangeEvent(player, this, oldValue, value).call();
			onlinePlayerStats.put(player, value);
		}
	}
	
	@Override
	public String getNameUnderscores() {
		return getName().replaceAll(" ", "_");
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof StatType))
			return false;
		return ((StatType)other).getID() == getID();
	}
	
	@Override
	public int hashCode() {
		return getID();
	}
}
