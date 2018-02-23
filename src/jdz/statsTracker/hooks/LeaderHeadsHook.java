
package jdz.statsTracker.hooks;

import java.util.Arrays;

import static org.bukkit.ChatColor.*;
import org.bukkit.entity.Player;

import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.defaults.StatTypePlayTime;
import lombok.Getter;
import me.robin.leaderheads.datacollectors.OnlineDataCollector;
import me.robin.leaderheads.objects.BoardType;

public class LeaderHeadsHook {
	@Getter private static final LeaderHeadsHook instance = new LeaderHeadsHook();

	private LeaderHeadsHook() {}

	public void addType(StatType type) {
		if (type instanceof StatTypePlayTime)
			new OnlineDataCollector(type.getNameUnderscores(), "gcStats", BoardType.TIME, "",
					"gcs rank " + type.getNameNoSpaces(),
					Arrays.asList(RED + "" + STRIKETHROUGH + "--------------",
							GREEN + "" + BOLD + "{name}", YELLOW + "{amount}",
							RED + "" + STRIKETHROUGH + "--------------")) {
				@Override
				public Double getScore(Player player) {
					return type.get(player) / 60;
				}
			};
		else
			new OnlineDataCollector(type.getNameUnderscores(), "gcStats", BoardType.DEFAULT, "",
					"gcs rank " + type.getNameNoSpaces(),
					Arrays.asList(RED + "" + STRIKETHROUGH + "--------------",
							GREEN + "" + BOLD + "{name}", YELLOW + "{amount}",
							RED + "" + STRIKETHROUGH + "--------------")) {
				@Override
				public Double getScore(Player player) {
					return type.get(player);
				}
			};
	}

}
