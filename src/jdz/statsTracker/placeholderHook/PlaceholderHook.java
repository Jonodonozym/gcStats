
package jdz.statsTracker.placeholderHook;

import org.bukkit.entity.Player;

import jdz.statsTracker.main.Main;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.util.SqlApi;
import me.clip.placeholderapi.external.EZPlaceholderHook;

public class PlaceholderHook extends EZPlaceholderHook{

	public PlaceholderHook() {
		super(Main.plugin, "gcStats");
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
		try{
			if (identifier.contains(":")){
				String[] args = identifier.split(":");
				StatType stat = StatType.valueOf(args[0].toUpperCase().replaceAll(" ", "_"));
				return stat.valueToString(SqlApi.getStat(player, stat.toString(), args[1]));
			}
			else{
				StatType stat = StatType.valueOf(identifier.toUpperCase().replaceAll(" ", "_"));
				return stat.valueToString(SqlApi.getStat(player, stat.toString()));
			}
		}
		catch(IllegalArgumentException e){ }
		return null;
	}
}
