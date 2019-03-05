
package jdz.statsTracker.hooks;

import org.bukkit.entity.Player;

import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceholderHook extends PlaceholderExpansion {

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
		StatType stat = StatsManager.getInstance().getType(identifier);
		if (stat == null)
			return null;

		return stat.valueToString(stat.get(player));
	}

	@Override
	public String getAuthor() {
		return "Jonodonozym";
	}

	@Override
	public String getIdentifier() {
		return "gcStats";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}
}
