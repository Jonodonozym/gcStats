
package jdz.statsTracker;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.misc.Config;
import jdz.bukkitUtils.misc.TimedTask;

public class GCStatsTrackerConfig {
	public static String serverName = "";
	public static Material serverIcon = Material.INK_SACK;
	public static short serverIconData = 2;

	public static int autoUpdateDelay = 100;
	public static int afkTime = 12000;

	public static String statsCommand = "gcs";
	public static String achCommand = "gca";

	public static boolean broadcastEnabled = false;
	public static int broadcastMinTime, broadcastMaxTime;
	public static String[] broadcastMessage = new String[1];

	public static boolean achievementFireworkEnabled = true;
	public static boolean achievementMessageEnabled = false;
	public static boolean achievementAwardPoints = true;
	public static boolean achievementPointsGlobal = false;

	private static TimedTask broadcastTask = null;
	
	public static List<String> servers = new ArrayList<String>();

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

		achievementFireworkEnabled = config.getBoolean("achievements.doFirework");
		achievementMessageEnabled = config.getBoolean("achievements.doMessage");
		achievementAwardPoints = config.getBoolean("achievements.awardPoints");
		achievementPointsGlobal = config.getBoolean("achievements.globalPoints");

		Material m = Material.GRASS;
		try {
			m = Material.valueOf(config.getString("server.icon"));
		} catch (Exception e) {
		}
		serverIconData = (short)config.getInt("server.iconDamage");
		serverIcon = m;
	}
}
