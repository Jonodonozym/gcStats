
package jdz.statsTracker.achievement;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import jdz.bukkitUtils.components.events.Listener;
import jdz.bukkitUtils.configuration.ConfigReloadEvent;
import jdz.statsTracker.GCStats;
import lombok.Getter;

public class AchievementConfig implements Listener {	
	@Getter private static boolean fireworkEnabled = true;
	@Getter private static boolean messageEnabled = false;
	@Getter private static boolean giveRewards = true;
	@Getter private static boolean pointsGlobal = false;
	
	@Getter private static ItemStack serverIcon = new ItemStack(Material.DIAMOND);
	
	@EventHandler
	public void onReload(ConfigReloadEvent event) {
		if (event.getPlugin() != GCStats.getInstance())
			return;
		
		FileConfiguration config = event.getConfig();

		fireworkEnabled = config.getBoolean("achievements.doFirework");
		messageEnabled = config.getBoolean("achievements.doMessage");
		giveRewards = config.getBoolean("achievements.giveRewards");
		pointsGlobal = config.getBoolean("achievements.globalPoints");

		Material m = Material.GRASS;
		try {
			m = Material.valueOf(config.getString("server.icon"));
		}
		catch (Exception e) {}
		short serverIconData = (short) config.getInt("server.iconDamage", 0);
		serverIcon = new ItemStack(m, 1, serverIconData);
	}

}
