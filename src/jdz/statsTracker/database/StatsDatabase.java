
package jdz.statsTracker.database;

import java.util.List;

import org.bukkit.OfflinePlayer;

import jdz.statsTracker.stats.StatType;

public interface StatsDatabase {
	public static StatsDatabase getInstance() {
		return StatsDatabaseMulti.getInstance();
	}

	public void addStatType(StatType type, boolean isEnabled);
	public List<String> getEnabledStats(String server);
	public List<String> getVisibleStats(String server);

	public void setStat(OfflinePlayer player, StatType statType, double newValue);
	public void addStat(OfflinePlayer player, StatType statType, double change);
	public void setStatSync(OfflinePlayer player, StatType statType, double newValue);
	public double getStat(OfflinePlayer player, StatType statType);
	public double getStat(OfflinePlayer player, String statType, String server);
	public List<String[]> getAllSorted(StatType type);
}
