
package jdz.statsTracker.achievement;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import jdz.bukkitUtils.configuration.AutoConfig;
import lombok.Getter;

public class AchievementConfig extends AutoConfig {
	@Getter private static boolean fireworkEnabled = true;
	@Getter private static boolean messageEnabled = false;
	@Getter private static boolean giveRewards = true;
	@Getter private static boolean pointsGlobal = false;

	private static Material serverIcon = Material.GRASS;

	public static ItemStack getServerIcon() {
		return new ItemStack(serverIcon);
	}

	public AchievementConfig(Plugin plugin) {
		super(plugin, "achievements");
	}
}
