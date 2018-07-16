
package jdz.statsTracker.achievement.database;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import jdz.statsTracker.GCStats;
import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.achievement.Achievement;
import jdz.statsTracker.achievement.AchievementManager;
import lombok.Getter;

public class AchievementDatabaseYML implements AchievementDatabase {
	@Getter private static final AchievementDatabaseYML instance = new AchievementDatabaseYML();

	private final File rootFolder = new File(GCStats.getInstance().getDataFolder(),
			"data" + File.separator + "achievements");

	private AchievementDatabaseYML() {
		if (!rootFolder.exists())
			rootFolder.mkdirs();
	}

	@Override
	public boolean hasPlayer(OfflinePlayer player, String server) {
		return getFile(player).exists();
	}

	@Override
	public void addPlayer(OfflinePlayer player) {
		FileConfiguration config = new YamlConfiguration();
		config.set("pts", 0);
		try {
			config.save(getFile(player));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setAchievementPoints(Player player, int points) {
		FileConfiguration config = YamlConfiguration.loadConfiguration(getFile(player));
		config.set("pts", points);
		try {
			config.save(getFile(player));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addAchievementPoints(Player player, int points) {
		FileConfiguration config = YamlConfiguration.loadConfiguration(getFile(player));
		config.set("pts", config.getInt("pts") + points);
		try {
			config.save(getFile(player));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getAchievementPoints(OfflinePlayer player, String server) {
		FileConfiguration config = YamlConfiguration.loadConfiguration(getFile(player));
		return config.getInt("pts");
	}

	@Override
	public void addAchievements(Achievement[] achievements) {}

	@Override
	public List<Achievement> getServerAchievements(String server) {
		return AchievementManager.getInstance().getAchievements();
	}

	@Override
	public boolean isAchieved(OfflinePlayer offlinePlayer, Achievement a, String server) {
		FileConfiguration config = YamlConfiguration.loadConfiguration(getFile(offlinePlayer));
		return config.contains(a.getName());
	}

	@Override
	public void setAchieved(Player player, Achievement a) {
		FileConfiguration config = YamlConfiguration.loadConfiguration(getFile(player));
		config.set(a.getName(), true);
		try {
			config.save(getFile(player));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<String> getServers() {
		return Arrays.asList(GCStatsConfig.serverName);
	}

	@Override
	public void setServerIcon(String server, ItemStack item) {}

	@Override
	public ItemStack getServerIcon(String server) {
		throw new UnsupportedOperationException();
	}

	private File getFile(OfflinePlayer player) {
		return new File(rootFolder, player.getUniqueId().toString());
	}

}
