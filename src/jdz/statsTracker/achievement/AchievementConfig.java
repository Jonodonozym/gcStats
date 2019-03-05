
package jdz.statsTracker.achievement;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import jdz.bukkitUtils.configuration.AutoConfig;
import jdz.statsTracker.GCStatsConfig;
import lombok.Getter;

public class AchievementConfig extends AutoConfig {
	@Getter private static boolean fireworkEnabled = true;
	@Getter private static boolean messageEnabled = false;
	@Getter private static boolean giveRewards = true;
	@Getter private static boolean pointsGlobal = false;

	public static ItemStack getServerIcon() {
		return new ItemStack(GCStatsConfig.getServerIcon());
	}

	public AchievementConfig(Plugin plugin) {
		super(plugin, "achievements");
	}
}
