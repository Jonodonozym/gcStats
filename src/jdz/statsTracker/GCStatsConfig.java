
package jdz.statsTracker;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import jdz.bukkitUtils.misc.Config;

public class GCStatsConfig {
	public static String serverName = "";

	public static int autoUpdateDelay = 100;
	public static int afkTime = 12000;

	public static String statsCommand = "gcs";
	public static String achCommand = "gca";

	public static List<String> servers = new ArrayList<String>();
	
	public static boolean SQLEnabled = true;

	public static void reloadConfig() {
		FileConfiguration config = Config.getConfig(GCStats.getInstance());

		servers.remove(serverName);
		serverName = config.getString("server.name");
		servers.add(serverName);
		
		SQLEnabled = config.getBoolean("data.SQLEnabled", true);
	}
}
