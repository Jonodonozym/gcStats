
package jdz.statsTracker.placeholderHook;

import org.bukkit.entity.Player;

import jdz.statsTracker.stats.StatsManager;
import jdz.statsTracker.GCStatsTracker;
import jdz.statsTracker.stats.StatType;
import me.clip.placeholderapi.external.EZPlaceholderHook;

public class PlaceholderHook extends EZPlaceholderHook{

	public PlaceholderHook() {
		super(GCStatsTracker.instance, "gcStats");
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
		StatType stat = StatsManager.getInstance().getType(identifier);
		if (stat == null)
			return null;
		return stat.valueToString(stat.get(player));
	}
}
