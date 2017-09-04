
package jdz.statsTracker.main;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.achievement.AchievementInventories;
import jdz.statsTracker.achievement.AchievementShop;
import jdz.statsTracker.stats.PlayTimeRecorder;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.util.SqlApi;
import jdz.statsTracker.util.TimedTask;

public class Config {
	public static String serverName = "";

	public static String dbURL = "";
	public static String dbName = "";
	public static String dbUsername = "";
	public static String dbPassword = "";

	public static Set<StatType> enabledStats = new HashSet<StatType>();

	public static int autoUpdateDelay = 6000;
	public static int afkTime = 12000;

	public static String statsCommand = "gcs";
	public static String achCommand = "gca";

	public static List<String> servers = new ArrayList<String>();

	public static boolean broadcastEnabled = false;
	public static int broadcastMinTime, broadcastMaxTime;
	public static String[] broadcastMessage = new String[1];

	public static boolean achievementFireworkEnabled = true;
	public static boolean achievementMessageEnabled = false;

	private static TimedTask broadcastTask = null, updateDistWalked = null, updatePlayTime = null;

	public static void reloadConfig() {
		File file = new File(Main.plugin.getDataFolder() + File.separator + "config.yml");
		if (!file.exists())
			Main.plugin.saveDefaultConfig();

		Main.plugin.reloadConfig();
		FileConfiguration config = Main.plugin.getConfig();

		serverName = config.getString("server.name");

		broadcastEnabled = config.getBoolean("broadcastInfo.enabled");
		broadcastMinTime = config.getInt("broadcastInfo.intervalMinutesMin");
		broadcastMaxTime = config.getInt("broadcastInfo.intervalMinutesMax");
		broadcastMessage = config.getStringList("broadcastInfo.message").toArray(broadcastMessage);
		
		if (broadcastEnabled){
			if (broadcastTask != null)
				broadcastTask.stop();
			broadcastTask = new TimedTask(broadcastMinTime*1200, broadcastMaxTime*1200, ()->{
				for(Player p: Main.plugin.getServer().getOnlinePlayers())
					p.sendMessage(broadcastMessage);
			});
		}

		achievementFireworkEnabled = config.getBoolean("achievementNotification.doFirework");
		achievementMessageEnabled = config.getBoolean("achievementNotification.doMessage");

		enabledStats.clear();
		for (StatType s : StatType.values())
			if (config.getBoolean("statsEnabled." + s))
				enabledStats.add(s);

		dbURL = config.getString("database.URL");
		dbName = config.getString("database.name");
		dbUsername = config.getString("database.username");
		dbPassword = config.getString("database.password");

		Material m = Material.GRASS;
		try {
			m = Material.valueOf(config.getString("server.icon"));
		} catch (Exception e) {
		}
		short damage = (short)config.getInt("server.iconDamage");

		if (dbURL.equals("") || dbName.equals("") || dbUsername.equals("") || dbPassword.equals("")) {
			Main.plugin.getLogger().info(
					"Some of the database lines in config.yml are empty, please fill in the config.yml and reload the plugin.");
		} else {
			Connection temp = SqlApi.open(Main.plugin.getLogger(), dbURL, 3306, dbName, dbUsername, dbPassword);
			if (temp != null) {
				SqlApi.ensureCorrectPointsTable();
				SqlApi.ensureCorrectStatTable();
				SqlApi.ensureCorrectServerMetaTable();
				SqlApi.ensureCorrectStatMetaTable();
				servers = SqlApi.getServers();
				SqlApi.setServerMeta(serverName, m, damage);
			} else
				enabledStats.clear();
		}
		
		if (updateDistWalked != null)
			updateDistWalked.stop();
		updateDistWalked = new TimedTask(Config.autoUpdateDelay, ()-> {
			if (Config.enabledStats.contains(StatType.DIST_WALKED))
				for(Player p: Main.plugin.getServer().getOnlinePlayers())
					SqlApi.setStat(p, StatType.DIST_WALKED, p.getStatistic(Statistic.WALK_ONE_CM)/100.0);
		});
		updateDistWalked.start();
		
		if (updatePlayTime != null)
			updatePlayTime.stop();
		
		updatePlayTime = new PlayTimeRecorder();
		updatePlayTime.start();

		AchievementData.reloadData();
		AchievementInventories.reload();
		AchievementShop.reload();
	}
}
