
package jdz.statsTracker.hooks;

import java.util.Arrays;

import static org.bukkit.ChatColor.*;
import org.bukkit.entity.Player;

import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.defaultTypes.StatTypePlayTime;
import lombok.Getter;
import me.robin.leaderheads.datacollectors.OnlineDataCollector;
import me.robin.leaderheads.objects.BoardType;

public class LeaderHeadsHook {
	@Getter private static final LeaderHeadsHook instance = new LeaderHeadsHook();

	private LeaderHeadsHook() {}

	public void addType(StatType type) {
		String name = "gcs_" + type.getNameUnderscores().toLowerCase();
		if (name.length() > 15)
			name = name.substring(0, 15);

		if (type instanceof StatTypePlayTime)
			new StatTypeDataCollecter(type, name) {
				@Override
				public Double getScore(Player player) {
					return type.get(player) / 60;
				}
			};
		else
			new StatTypeDataCollecter(type, name) {
				@Override
				public Double getScore(Player player) {
					return type.get(player);
				}
			};
	}

	private abstract class StatTypeDataCollecter extends OnlineDataCollector {
		public StatTypeDataCollecter(StatType type, String name) {
			super(name, "gcStats", BoardType.DEFAULT, type.getName(), "gcs top " + type.getNameNoSpaces(),
					Arrays.asList(RED + "" + STRIKETHROUGH + "----------", GREEN + "{name}",
							YELLOW + "{amount}", RED + "" + STRIKETHROUGH + "----------"));
		}
	}

}
