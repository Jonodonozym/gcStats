
package jdz.statsTracker.placeholderHook;

import org.bukkit.entity.Player;

import jdz.statsTracker.stats.StatsManager;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import jdz.statsTracker.GCStatsTracker;
import jdz.statsTracker.stats.StatType;

public class PlaceholderHook extends EZPlaceholderHook {

	public PlaceholderHook() {
		super(GCStatsTracker.instance, "gcStats");
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {

		/*
		 * String statID = identifier;
		 * String topPlayer = "";
		 * 
		 * int topIndex = identifier.indexOf("top");
		 * if (topIndex != -1) {
		 * String statString = identifier.substring(0, topIndex);
		 * String
		 * }
		 */

		StatType stat = StatsManager.getInstance().getType(identifier);
		if (stat == null)
			return null;

		return stat.valueToString(stat.get(player));
	}
}
