
package jdz.statsTracker.broadcaster;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.Plugin;

import jdz.bukkitUtils.configuration.AutoConfig;
import lombok.Getter;

public class BroadcasterConfig extends AutoConfig {
	@Getter private static boolean enabled = false;
	@Getter private static int minTime, maxTime;
	private static List<String> message = new ArrayList<>();

	public static String[] getMessage() {
		return message.toArray(new String[0]);
	}

	public BroadcasterConfig(Plugin plugin) {
		super(plugin, "broadcastInfo");
	}
}
