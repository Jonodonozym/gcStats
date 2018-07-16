
package jdz.statsTracker.broadcaster;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;

import jdz.bukkitUtils.events.Listener;
import jdz.bukkitUtils.events.custom.ConfigReloadEvent;
import jdz.statsTracker.GCStats;
import lombok.Getter;

public class BroadcasterConfig implements Listener {
	@Getter private static boolean enabled = false;
	@Getter private static int minTime, maxTime;
	@Getter private static String[] message = new String[1];
	
	@EventHandler
	public void onConfigReload(ConfigReloadEvent event) {
		if (event.getPlugin() != GCStats.getInstance())
			return;
		
		FileConfiguration config = event.getConfig();
		
		enabled = config.getBoolean("broadcastInfo.enabled");
		minTime = config.getInt("broadcastInfo.intervalMinutesMin");
		maxTime = config.getInt("broadcastInfo.intervalMinutesMax");
		message = config.getStringList("broadcastInfo.message").toArray(message);

		for (int i = 0; i < message.length; i++)
			message[i] = message[i].replaceAll("&([0-9a-f])", "\u00A7$1");
	}
}
