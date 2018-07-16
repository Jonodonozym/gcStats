
package jdz.statsTracker.achievement;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;

import jdz.bukkitUtils.events.Listener;
import jdz.bukkitUtils.events.custom.ConfigReloadEvent;
import jdz.statsTracker.GCStats;
import lombok.Getter;

public class AchievementConfig implements Listener {	
	@Getter private static boolean fireworkEnabled = true;
	@Getter private static boolean messageEnabled = false;
	@Getter private static boolean giveRewards = true;
	@Getter private static boolean pointsGlobal = false;
	
	@EventHandler
	public void onReload(ConfigReloadEvent event) {
		if (event.getPlugin() != GCStats.getInstance())
			return;
		
		FileConfiguration config = event.getConfig();

		fireworkEnabled = config.getBoolean("achievements.doFirework");
		messageEnabled = config.getBoolean("achievements.doMessage");
		giveRewards = config.getBoolean("achievements.giveRewards");
		pointsGlobal = config.getBoolean("achievements.globalPoints");
	}

}
