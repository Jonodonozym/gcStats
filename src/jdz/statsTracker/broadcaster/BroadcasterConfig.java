
package jdz.statsTracker.broadcaster;

import java.util.Arrays;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;

import jdz.bukkitUtils.configuration.AutoConfig;
import jdz.bukkitUtils.configuration.ConfigReloadEvent;
import jdz.bukkitUtils.utils.ColorUtils;
import lombok.Getter;

public class BroadcasterConfig extends AutoConfig {
	@Getter private static boolean enabled = false;
	@Getter private static int intervalMinutesMin, intervalMinutesMax;
	@Getter private static List<String> message = Arrays.asList();

	public BroadcasterConfig(Plugin plugin) {
		super(plugin, "broadcastInfo");
	}

	@EventHandler
	public void onConfigReload(ConfigReloadEvent event) {
		super.onConfigReload(event);
		message = ColorUtils.translate(message);
	}
}
