
package jdz.statsTracker.stats;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;

import jdz.statsTracker.GCStatsConfig;

public interface StatsDatabase {
	public static StatsDatabase getInstance() {
		if (GCStatsConfig.SQLEnabled)
			return StatsDatabaseSQL.getInstance();
		return StatsDatabaseYML.getInstance();
	}

	public int countEntries(String server);

	public void addStatType(StatType type, boolean isEnabled);

	public List<String> getEnabledStats(String server);

	public List<String> getVisibleStats(String server);

	public default boolean hasPlayer(OfflinePlayer player) {
		return hasPlayer(player, GCStatsConfig.serverName);
	}

	public boolean hasPlayer(OfflinePlayer player, String server);

	public void addPlayer(OfflinePlayer player);
	
	public default void setStat(OfflinePlayer player, StatType statType, double newValue) {
		Map<StatType, Double> statToValue = new HashMap<StatType, Double>();
		statToValue.put(statType, newValue);
		setStats(player, statToValue);
	}

	public void setStats(OfflinePlayer player, Map<StatType, Double> statToValue);
	
	public void addStat(OfflinePlayer player, StatType statType, double change);

	public Map<StatType, Double> getStats(OfflinePlayer player, Collection<? extends StatType> statTypes);

	public Map<String, Double> getStats(OfflinePlayer player, Collection<String> statTypes, String server);

	public default double getStat(OfflinePlayer player, String statType, String server){
		return getStats(player, Arrays.asList(statType), server).get(statType);
	}

	public Map<String, Double> getAllSorted(StatType type);
}
