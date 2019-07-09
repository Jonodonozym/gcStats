
package jdz.statsTracker.hooks;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsManager;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

@RequiredArgsConstructor
public class PlaceholderHook extends PlaceholderExpansion {
	private final Plugin plugin;

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
		StatType stat = StatsManager.getInstance().getType(identifier);
		if (stat == null)
			return null;

		return stat.valueToString(stat.get(player));
	}

	@Override
	public String getAuthor() {
		List<String> authors = plugin.getDescription().getAuthors();
		if (authors != null && !authors.isEmpty())
			return authors.get(0);
		return null;
	}

	@Override
	public String getIdentifier() {
		return plugin.getName();
	}

	@Override
	public String getVersion() {
		return plugin.getDescription().getVersion();
	}
}
