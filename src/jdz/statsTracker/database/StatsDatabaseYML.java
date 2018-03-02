
package jdz.statsTracker.database;

import java.util.List;

import org.bukkit.OfflinePlayer;

import jdz.statsTracker.stats.StatType;
import lombok.Getter;

class StatsDatabaseYML implements StatsDatabase{
	@Getter private static final StatsDatabaseYML instance = new StatsDatabaseYML();

	@Override
	public void addStatType(StatType type, boolean isEnabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getEnabledStats(String server) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getVisibleStats(String server) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStat(OfflinePlayer player, StatType statType, double newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addStat(OfflinePlayer player, StatType statType, double change) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStatSync(OfflinePlayer player, StatType statType, double newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getStat(OfflinePlayer player, StatType statType) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getStat(OfflinePlayer player, String statType, String server) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<String[]> getAllSorted(StatType type) {
		// TODO Auto-generated method stub
		return null;
	}

}
