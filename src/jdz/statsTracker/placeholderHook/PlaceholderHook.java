
package jdz.statsTracker.placeholderHook;

import org.bukkit.entity.Player;

import jdz.statsTracker.stats.StatsDatabase;
import jdz.statsTracker.GCStatsTracker;
import jdz.statsTracker.stats.StatType;
import me.clip.placeholderapi.external.EZPlaceholderHook;

public class PlaceholderHook extends EZPlaceholderHook{

	public PlaceholderHook() {
		super(GCStatsTracker.instance, "gcStats");
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
		try{
			StatType stat = StatType.valueOf(identifier.toUpperCase().replaceAll(" ", "_"));
			return stat.valueToString(StatsDatabase.getInstance().getStat(player, stat));
		}
		catch(IllegalArgumentException e){ }
		return null;
	}
}
