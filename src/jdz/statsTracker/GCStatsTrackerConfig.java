
package jdz.statsTracker;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.misc.Config;
import jdz.bukkitUtils.misc.TimedTask;
import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.achievement.AchievementDatabase;
import jdz.statsTracker.achievement.AchievementInventories;
import jdz.statsTracker.achievement.AchievementShop;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsDatabase;

public class GCStatsTrackerConfig {
	public static String serverName = "";
	public static List<StatType> enabledStats = new ArrayList<StatType>();

	public static int autoUpdateDelay = 100;
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
		FileConfiguration config = Config.getConfig(GCStatsTracker.instance);

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
			broadcastTask = new TimedTask(GCStatsTracker.instance, broadcastMaxTime*1200, ()->{
				for(Player p: GCStatsTracker.instance.getServer().getOnlinePlayers())
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

		Material m = Material.GRASS;
		try {
			m = Material.valueOf(config.getString("server.icon"));
		} catch (Exception e) {
		}
		final short damage = (short)config.getInt("server.iconDamage");
		final Material m2 = m;
		
		StatsDatabase.getInstance().runOnConnect(()->{
			servers = StatsDatabase.getInstance().getServers();
			AchievementDatabase.getInstance().setServerIcon(serverName, m2, damage);
			
			if (updateGeneralStats != null)
				updateGeneralStats.stop();
			updateGeneralStats = new TimedTask(GCStatsTracker.instance, GCStatsTrackerConfig.autoUpdateDelay, ()-> {
				if (GCStatsTrackerConfig.enabledStats.contains(StatType.DIST_WALKED))
					for(Player p: GCStatsTracker.instance.getServer().getOnlinePlayers())
						StatsDatabase.getInstance().setStat(p, StatType.DIST_WALKED, p.getStatistic(Statistic.WALK_ONE_CM)/100.0);
			});
			updateGeneralStats.start();
			
			AchievementData.reloadData();
			AchievementInventories.reload();
			AchievementShop.reload();
		});
	}
}
