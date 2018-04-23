
package jdz.statsTracker.database;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import jdz.bukkitUtils.misc.Config;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.stats.NoSaveStatType;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsManager;
import lombok.Getter;

class StatsDatabaseYML implements StatsDatabase {
	@Getter private static final StatsDatabaseYML instance = new StatsDatabaseYML();

	private final File file = Config.getConfigFile(GCStats.instance, "data/playerStats.yml");
	private final FileConfiguration config = Config.getConfig(GCStats.instance, "data/playerStats.yml");
	private static final int AUTOSAVE_TICKS = 5 * 60 * 20;

	private StatsDatabaseYML() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(GCStats.instance, () -> {
			try {
				config.save(file);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}, AUTOSAVE_TICKS, AUTOSAVE_TICKS);
	}
	
	@Override
	public int countEntries(String server) {
		return -1;
	}

	@Override
	public void addStatType(StatType type, boolean isEnabled) {}

	@Override
	public List<String> getEnabledStats(String server) {
		if (server.equals(GCStatsConfig.serverName)) {
			List<String> types = new ArrayList<String>();
			for (StatType type : StatsManager.getInstance().enabledStats())
				types.add(type.getName());
			return types;
		}

		return new ArrayList<String>();
	}

	@Override
	public List<String> getVisibleStats(String server) {
		if (server.equals(GCStatsConfig.serverName)) {
			List<String> types = new ArrayList<String>();
			for (StatType type : StatsManager.getInstance().enabledStats())
				if (type.isVisible())
					types.add(type.getName());
			return types;
		}
		else
			return new ArrayList<String>();
	}

	@Override
	public void setStat(OfflinePlayer player, StatType statType, double newValue) {
		if (statType instanceof NoSaveStatType)
			return;
		config.set(player.getName() + "." + statType.getNameUnderscores(), newValue);
	}

	@Override
	public void addStat(OfflinePlayer player, StatType statType, double change) {
		if (statType instanceof NoSaveStatType)
			return;
		config.set(player.getName() + "." + statType.getNameUnderscores(), getStat(player, statType) + change);
	}

	@Override
	public void setStatSync(OfflinePlayer player, StatType statType, double newValue) {
		if (statType instanceof NoSaveStatType)
			return;
		setStat(player, statType, newValue);
	}

	@Override
	public double getStat(OfflinePlayer player, StatType statType) {
		if (config.contains(player.getName() + "." + statType.getNameUnderscores()))
			return config.getDouble(player.getName() + "." + statType.getNameUnderscores());
		return 0;
	}

	@Override
	public double getStat(OfflinePlayer player, String statType, String server) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Double> getAllSorted(StatType type) {
		Map<String, Double> all = new HashMap<String, Double>();

		if (type instanceof NoSaveStatType)
			return all;

		for (String player : config.getKeys(false))
			all.put(player, config.getDouble(player + "." + type.getNameUnderscores()));

		return all.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	@Override
	public void onShutDown() {
		try {
			config.save(file);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasPlayer(OfflinePlayer player, String server) {
		// TODO Auto-generated method stub
		return false;
	}
}
