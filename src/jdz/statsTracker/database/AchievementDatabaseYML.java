
package jdz.statsTracker.database;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import jdz.bukkitUtils.misc.Config;
import jdz.bukkitUtils.misc.utils.ItemUtils;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.achievement.Achievement;
import jdz.statsTracker.achievement.AchievementManager;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

class AchievementDatabaseYML implements AchievementDatabase {
	@Getter private static final AchievementDatabaseYML instance = new AchievementDatabaseYML();

	private final FileConfiguration achConfig = Config.getConfig(GCStats.instance, "data/playerAchievements.yml");
	private final FileConfiguration pointsConfig = Config.getConfig(GCStats.instance,
			"data/playerAchievementPoints.yml");

	private final File achFile = Config.getConfigFile(GCStats.instance, "data/playerAchievements.yml");
	private final File pointsFile = Config.getConfigFile(GCStats.instance, "data/playerAchievementPoints.yml");

	private static final int AUTOSAVE_TICKS = 5 * 60 * 20;

	private AchievementDatabaseYML() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(GCStats.instance, () -> {
			try {
				achConfig.save(achFile);
				pointsConfig.save(pointsFile);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}, AUTOSAVE_TICKS, AUTOSAVE_TICKS);
	}

	@Override
	public void setAchievementPoints(Player player, int points) {
		pointsConfig.set(player.getName(), points);
	}

	@Override
	public void addAchievementPoints(Player player, int points) {
		setAchievementPoints(player, getAchievementPoints(player) + points);
	}

	@Override
	public int getAchievementPoints(OfflinePlayer player) {
		if (!pointsConfig.contains(player.getName()))
			return 0;
		return pointsConfig.getInt(player.getName());
	}

	@Override
	public int getAchievementPoints(OfflinePlayer player, String server) {
		if (GCStatsConfig.serverName.equals(server))
			return getAchievementPoints(player);
		return 0;
	}

	@Override
	public void addAchievements(Achievement[] achievements) {}

	@Override
	public List<Achievement> getServerAchievements(String server) {
		List<Achievement> achievements = new ArrayList<Achievement>();
		if (GCStatsConfig.serverName.equals(server))
			achievements.addAll(AchievementManager.getInstance().getAchievements());
		return achievements;
	}

	@Override
	public boolean isAchieved(OfflinePlayer offlinePlayer, Achievement a) {
		return achConfig.contains(offlinePlayer.getName() + "." + a.getName());
	}

	@Override
	public boolean isAchieved(OfflinePlayer offlinePlayer, Achievement a, String server) {
		if (GCStatsConfig.serverName.equals(server))
			return isAchieved(offlinePlayer, a);
		return false;
	}

	@Override
	public void setAchieved(Player player, Achievement a) {
		achConfig.set(player.getName() + "." + a.getName(), true);
	}

	@Override
	public List<String> getServers() {
		return Arrays.asList(GCStatsConfig.serverName);
	}

	private ItemStack serverIcon = new ItemStack(Material.AIR);

	@Override
	public void setServerIcon(String server, Material m, short damage) {
		serverIcon = new ItemStack(m, 1, damage);
		ItemUtils.setName(serverIcon, ChatColor.GREEN + server.replaceAll("_", " "));
	}

	@Override
	public ItemStack getServerIcon(String server) {
		return serverIcon;
	}

	@Override
	public boolean hasPlayer(OfflinePlayer player, String server) {
		// TODO Auto-generated method stub
		return false;
	}

}
