
package jdz.statsTracker.achievement.database;

import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.achievement.Achievement;

public interface AchievementDatabase {
	public static AchievementDatabase getInstance() {
		if (GCStatsConfig.SQLEnabled)
			return AchievementDatabaseSQL.getInstance();
		return AchievementDatabaseYML.getInstance();
	}

	public default boolean hasPlayer(OfflinePlayer player) {
		return hasPlayer(player, GCStatsConfig.serverName);
	}

	public boolean hasPlayer(OfflinePlayer player, String server);

	public void addPlayer(OfflinePlayer player);

	public void setAchievementPoints(Player player, int points);

	public void addAchievementPoints(Player player, int points);

	public default int getAchievementPoints(OfflinePlayer player) {
		return getAchievementPoints(player, GCStatsConfig.serverName);
	};

	public int getAchievementPoints(OfflinePlayer player, String server);

	public void addAchievements(Achievement[] achievements);

	public List<Achievement> getServerAchievements(String server);

	public default boolean isAchieved(OfflinePlayer offlinePlayer, Achievement a) {
		return isAchieved(offlinePlayer, a, GCStatsConfig.serverName);
	}

	public boolean isAchieved(OfflinePlayer offlinePlayer, Achievement a, String server);

	public void setAchieved(Player player, Achievement a);

	public List<String> getServers();

	public default boolean hasServer(String server) {
		return getServers().contains(server);
	}

	public void setServerIcon(String server, ItemStack stack);

	public ItemStack getServerIcon(String server);
}
