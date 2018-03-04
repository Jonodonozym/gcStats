
package jdz.statsTracker.database;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.achievement.Achievement;
import lombok.Getter;

class AchievementDatabaseMulti implements AchievementDatabase {
	@Getter private static final AchievementDatabaseMulti instance = new AchievementDatabaseMulti();

	@Override
	public void setAchievementPoints(Player player, int points) {
		AchievementDatabaseSQL.getInstance().setAchievementPoints(player, points);
		AchievementDatabaseYML.getInstance().setAchievementPoints(player, points);
	}

	@Override
	public void addAchievementPoints(Player player, int points) {
		AchievementDatabaseSQL.getInstance().addAchievementPoints(player, points);
		AchievementDatabaseYML.getInstance().addAchievementPoints(player, points);
	}

	@Override
	public int getAchievementPoints(OfflinePlayer player) {
		return AchievementDatabaseYML.getInstance().getAchievementPoints(player);
	}

	@Override
	public int getAchievementPoints(OfflinePlayer player, String server) {
		if (!server.equals(GCStatsConfig.serverName) && AchievementDatabaseSQL.getInstance().isConnected())
			return AchievementDatabaseSQL.getInstance().getAchievementPoints(player, server);
		return AchievementDatabaseYML.getInstance().getAchievementPoints(player, server);
	}

	@Override
	public void addAchievements(Achievement[] achievements) {
		AchievementDatabaseSQL.getInstance().addAchievements(achievements);
		AchievementDatabaseYML.getInstance().addAchievements(achievements);
	}

	@Override
	public List<Achievement> getServerAchievements(String server) {
		if (!server.equals(GCStatsConfig.serverName) && AchievementDatabaseSQL.getInstance().isConnected())
			return AchievementDatabaseSQL.getInstance().getServerAchievements(server);
		return AchievementDatabaseYML.getInstance().getServerAchievements(server);
	}

	@Override
	public boolean isAchieved(OfflinePlayer offlinePlayer, Achievement a) {
		return AchievementDatabaseYML.getInstance().isAchieved(offlinePlayer, a);
	}

	@Override
	public boolean isAchieved(OfflinePlayer offlinePlayer, Achievement a, String server) {
		if (!server.equals(GCStatsConfig.serverName) && AchievementDatabaseSQL.getInstance().isConnected())
			return AchievementDatabaseSQL.getInstance().isAchieved(offlinePlayer, a, server);
		return AchievementDatabaseYML.getInstance().isAchieved(offlinePlayer, a, server);
	}

	@Override
	public void setAchieved(Player player, Achievement a) {
		AchievementDatabaseSQL.getInstance().setAchieved(player, a);
		AchievementDatabaseYML.getInstance().setAchieved(player, a);
	}

	@Override
	public List<String> getServers() {
		if (AchievementDatabaseSQL.getInstance().isConnected())
			return AchievementDatabaseSQL.getInstance().getServers();
		return AchievementDatabaseYML.getInstance().getServers();
	}

	@Override
	public void setServerIcon(String server, Material m, short damage) {
		AchievementDatabaseSQL.getInstance().setServerIcon(server, m, damage);
		AchievementDatabaseYML.getInstance().setServerIcon(server, m, damage);
	}

	@Override
	public ItemStack getServerIcon(String server) {
		if (AchievementDatabaseSQL.getInstance().isConnected())
			return AchievementDatabaseSQL.getInstance().getServerIcon(server);
		return AchievementDatabaseYML.getInstance().getServerIcon(server);
	}

}
