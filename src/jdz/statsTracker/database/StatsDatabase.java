
package jdz.statsTracker.database;

import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;

import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.stats.StatType;

public interface StatsDatabase {
	public static StatsDatabase getInstance() {
		return StatsDatabaseSQL.getInstance();
	}

	public int countEntries(String server);
	
	public void addStatType(StatType type, boolean isEnabled);

	public List<String> getEnabledStats(String server);

	public List<String> getVisibleStats(String server);

	public default boolean hasPlayer(OfflinePlayer player) {
		if (player.isOnline() || player.hasPlayedBefore())
			return true;
		return hasPlayer(player, GCStatsConfig.serverName);
	}
	
	public boolean hasPlayer(OfflinePlayer player, String server);

	public void setStat(OfflinePlayer player, StatType statType, double newValue);

	public void addStat(OfflinePlayer player, StatType statType, double change);

	public void setStatSync(OfflinePlayer player, StatType statType, double newValue);

	public double getStat(OfflinePlayer player, StatType statType);

	public double getStat(OfflinePlayer player, String statType, String server);

	public Map<String, Double> getAllSorted(StatType type);

	public void onShutDown();
}
