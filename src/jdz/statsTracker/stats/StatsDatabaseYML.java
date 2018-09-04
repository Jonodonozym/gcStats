
package jdz.statsTracker.stats;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import jdz.statsTracker.GCStats;
import lombok.Getter;

public class StatsDatabaseYML implements StatsDatabase {
	@Getter private static final StatsDatabaseYML instance = new StatsDatabaseYML();

	private final File rootFolder = new File(GCStats.getInstance().getDataFolder(), "data"+File.separator+"stats");

	private StatsDatabaseYML() {
		if (!rootFolder.exists())
			rootFolder.mkdirs();
	}

	@Override
	public int countEntries(String server) {
		return rootFolder.listFiles().length - 1;
	}

	@Override
	public void addStatType(StatType type, boolean isEnabled) {}

	@Override
	public List<String> getEnabledStats(String server) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> getVisibleStats(String server) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasPlayer(OfflinePlayer player, String server) {
		return getFile(player).exists();
	}

	@Override
	public void addPlayer(OfflinePlayer player) {
		FileConfiguration config = new YamlConfiguration();
		for (StatType type : StatsManager.getInstance().getBufferedTypes())
			config.set(type.getName(), type.getDefault());
		config.set("playerName", player.getName());

		try {
			config.save(getFile(player));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setStats(OfflinePlayer player, Map<StatType, Double> statToValue) {
		FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(getFile(player));

		playerConfig.set("playerName", player.getName());
		for (StatType type : statToValue.keySet())
			playerConfig.set(type.getName(), statToValue);

		try {
			playerConfig.save(getFile(player));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addStat(OfflinePlayer player, StatType statType, double change) {
		FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(getFile(player));
		double value = playerConfig.getDouble(statType.getName(), statType.getDefault());
		playerConfig.set(statType.getName(), value + change);

		try {
			playerConfig.save(getFile(player));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Map<StatType, Double> getStats(OfflinePlayer player, Collection<? extends StatType> statTypes) {
		Map<StatType, Double> statToValue = new HashMap<StatType, Double>();
		FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(getFile(player));

		for (StatType type : statTypes)
			statToValue.put(type, playerConfig.getDouble(type.getName(), type.getDefault()));

		return statToValue;
	}

	@Override
	public Map<String, Double> getStats(OfflinePlayer player, Collection<String> statTypes, String server) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Double> getAllSorted(StatType type) {
		Map<String, Double> playerToValue = new HashMap<String, Double>();
		for (File file : rootFolder.listFiles()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			String name = config.getString("playerName");
			double value = config.getDouble(type.getName());
			playerToValue.put(name, value);
		}
		return playerToValue.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
						LinkedHashMap::new));
	}

	private File getFile(OfflinePlayer player) {
		return new File(rootFolder, player.getUniqueId().toString());
	}

}
