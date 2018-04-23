
package jdz.statsTracker.database;

import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;

import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsManager;
import lombok.Getter;

class StatsDatabaseMulti implements StatsDatabase {
	@Getter private static final StatsDatabaseMulti instance = new StatsDatabaseMulti();

	private StatsDatabaseMulti() {
		StatsDatabaseYML.getInstance();
		StatsDatabaseSQL.getInstance();
	}
	
	@Override
	public int countEntries(String server) {
		if (StatsDatabaseSQL.getInstance().isConnected())
			return StatsDatabaseSQL.getInstance().countEntries(server);
		return StatsDatabaseYML.getInstance().countEntries(server);
	}

	@Override
	public void addStatType(StatType type, boolean isEnabled) {
		StatsDatabaseYML.getInstance().addStatType(type, isEnabled);
		StatsDatabaseSQL.getInstance().addStatType(type, isEnabled);
	}

	@Override
	public List<String> getEnabledStats(String server) {
		if (!server.equals(GCStatsConfig.serverName) && StatsDatabaseSQL.getInstance().isConnected())
			return StatsDatabaseSQL.getInstance().getEnabledStats(server);
		return StatsDatabaseYML.getInstance().getEnabledStats(server);
	}

	@Override
	public List<String> getVisibleStats(String server) {
		if (!server.equals(GCStatsConfig.serverName) && StatsDatabaseSQL.getInstance().isConnected())
			return StatsDatabaseSQL.getInstance().getVisibleStats(server);
		return StatsDatabaseYML.getInstance().getVisibleStats(server);
	}
	
	@Override
	public boolean hasPlayer(OfflinePlayer player, String server) {
		if (StatsDatabaseSQL.getInstance().isConnected())
			return StatsDatabaseSQL.getInstance().hasPlayer(player, server);
		return StatsDatabaseYML.getInstance().hasPlayer(player, server);
	}

	@Override
	public void setStat(OfflinePlayer player, StatType statType, double newValue) {
		StatsDatabaseSQL.getInstance().setStat(player, statType, newValue);
		StatsDatabaseYML.getInstance().setStat(player, statType, newValue);
	}

	@Override
	public void addStat(OfflinePlayer player, StatType statType, double change) {
		StatsDatabaseSQL.getInstance().addStat(player, statType, change);
		StatsDatabaseYML.getInstance().addStat(player, statType, change);
	}

	@Override
	public void setStatSync(OfflinePlayer player, StatType statType, double newValue) {
		StatsDatabaseSQL.getInstance().setStatSync(player, statType, newValue);
		StatsDatabaseYML.getInstance().setStatSync(player, statType, newValue);
	}

	@Override
	public double getStat(OfflinePlayer player, StatType statType) {
		if (StatsDatabaseSQL.getInstance().isConnected())
			return StatsDatabaseSQL.getInstance().getStat(player, statType);
		return StatsDatabaseYML.getInstance().getStat(player, statType);
	}

	@Override
	public double getStat(OfflinePlayer player, String statType, String server) {
		if (server.equals(GCStatsConfig.serverName)) {
			StatType type = StatsManager.getInstance().getType(statType);
			if (type != null)
				return getStat(player, type);
		}
		if (StatsDatabaseSQL.getInstance().isConnected())
			return StatsDatabaseSQL.getInstance().getStat(player, statType, server);
		return 0;
	}

	@Override
	public Map<String, Double> getAllSorted(StatType type) {
		if (StatsDatabaseSQL.getInstance().isConnected())
			return StatsDatabaseSQL.getInstance().getAllSorted(type);
		return StatsDatabaseYML.getInstance().getAllSorted(type);
	}

	@Override
	public void onShutDown() {
		StatsDatabaseYML.getInstance().onShutDown();
		StatsDatabaseSQL.getInstance().onShutDown();
	}

}
