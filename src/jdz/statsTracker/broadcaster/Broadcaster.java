
package jdz.statsTracker.broadcaster;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.components.Random;
import jdz.statsTracker.GCStats;

public class Broadcaster {
	private static int nextBroadcast = 0;

	public static void init() {
		Bukkit.getScheduler().runTaskTimer(GCStats.getInstance(), () -> {
			if (!BroadcasterConfig.isEnabled())
				return;

			if (--nextBroadcast > 0)
				return;

			nextBroadcast = Random.nextInt(BroadcasterConfig.getMaxTime() - BroadcasterConfig.getMinTime())
					+ BroadcasterConfig.getMinTime();
			for (Player p : Bukkit.getOnlinePlayers())
				p.sendMessage(BroadcasterConfig.getMessage());

		}, 1, 1);
	}
}
