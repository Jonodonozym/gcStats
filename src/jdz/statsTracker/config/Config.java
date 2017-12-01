
package jdz.statsTracker.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.misc.TimedTask;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.achievement.AchievementInventories;
import jdz.statsTracker.achievement.AchievementShop;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsDatabase;

public class Config {
	public static String serverName = "";
	public static Set<StatType> enabledStats = new HashSet<StatType>();

	public static int autoUpdateDelay = 600;
	public static int afkTime = 12000;

	public static String statsCommand = "gcs";
	public static String achCommand = "gca";

	public static List<String> servers = new ArrayList<String>();

	public static boolean broadcastEnabled = false;
	public static int broadcastMinTime, broadcastMaxTime;
	public static String[] broadcastMessage = new String[1];

	public static boolean achievementFireworkEnabled = true;
	public static boolean achievementMessageEnabled = false;

	private static TimedTask broadcastTask = null, updateGeneralStats = null;

	public static void reloadConfig() {
		File file = new File(GCStats.plugin.getDataFolder() + File.separator + "config.yml");
		if (!file.exists())
			GCStats.plugin.saveDefaultConfig();

		GCStats.plugin.reloadConfig();
		FileConfiguration config = GCStats.plugin.getConfig();

		serverName = config.getString("server.name");

		broadcastEnabled = config.getBoolean("broadcastInfo.enabled");
		broadcastMinTime = config.getInt("broadcastInfo.intervalMinutesMin");
		broadcastMaxTime = config.getInt("broadcastInfo.intervalMinutesMax");
		broadcastMessage = config.getStringList("broadcastInfo.message").toArray(broadcastMessage);
		for (int i=0; i<broadcastMessage.length; i++)
			broadcastMessage[i] = broadcastMessage[i].replaceAll("&([0-9a-f])", "\u00A7$1");
		
		if (broadcastEnabled){
			if (broadcastTask != null)
				broadcastTask.stop();
			broadcastTask = new TimedTask(GCStats.plugin, broadcastMinTime*1200, ()->{
				for(Player p: GCStats.plugin.getServer().getOnlinePlayers())
					p.sendMessage(broadcastMessage);
			});
			broadcastTask.start();
		}

		achievementFireworkEnabled = config.getBoolean("achievementNotification.doFirework");
		achievementMessageEnabled = config.getBoolean("achievementNotification.doMessage");

		enabledStats.clear();
		for (StatType s : StatType.values())
			if (config.getBoolean("statsEnabled." + s))
				enabledStats.add(s);
		
		if (StatsDatabase.getInstance().getApi().isConnected())
			afterReload(config);
		else
			StatsDatabase.getInstance().getApi().runOnConnect(()->{ afterReload(config); });
	}
	
	private static void afterReload(FileConfiguration config) {
		servers = StatsDatabase.getInstance().getServers();
		


		Material m = Material.GRASS;
		try {
			m = Material.valueOf(config.getString("server.icon"));
		} catch (Exception e) {
		}
		
		final short damage = (short)config.getInt("server.iconDamage");
		final Material m2 = m;
		
		StatsDatabase.getInstance().setServerMeta(serverName, m2, damage);
		
		if (updateGeneralStats != null)
			updateGeneralStats.stop();
		
		updateGeneralStats = new TimedTask(GCStats.plugin, Config.autoUpdateDelay, ()-> {
			if (Config.enabledStats.contains(StatType.DIST_WALKED))
				for(Player p: GCStats.plugin.getServer().getOnlinePlayers())
					StatsDatabase.getInstance().setStat(p, StatType.DIST_WALKED, p.getStatistic(Statistic.WALK_ONE_CM)/100.0);
		});
		updateGeneralStats.start();
		
		AchievementData.reloadData(GCStats.plugin);
		AchievementInventories.reload();
		AchievementShop.reload(GCStats.plugin);
	}
}
