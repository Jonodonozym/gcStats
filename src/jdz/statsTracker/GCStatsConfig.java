
package jdz.statsTracker;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import jdz.bukkitUtils.configuration.Config;
import lombok.Getter;

public class GCStatsConfig {
	public static String serverName = "";
	public static List<String> servers = new ArrayList<>();
	public static boolean SQLEnabled = true;

	@Getter private static Material serverIcon = Material.GRASS;

	public static void reloadConfig() {
		FileConfiguration config = Config.getConfig(GCStats.getInstance());

		servers.remove(serverName);
		serverName = config.getString("server.name");
		servers.add(serverName);
		
		serverIcon = Material.getMaterial(config.getString("server.icon"));

		SQLEnabled = config.getBoolean("data.SQLEnabled", true);
	}
}
