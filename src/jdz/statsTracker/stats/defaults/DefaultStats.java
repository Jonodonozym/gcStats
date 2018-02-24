
package jdz.statsTracker.stats.defaults;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;

import jdz.statsTracker.stats.StatType;
import lombok.Getter;

public class DefaultStats {
	@Getter private static final DefaultStats instance = new DefaultStats();

	@Getter private final Set<StatType> all = new HashSet<StatType>(Arrays.asList(StatTypeBlocksMined.getInstance(),
			StatTypeBlocksPlaced.getInstance(), StatTypeDeaths.getInstance(), StatTypeDistanceWalked.getInstance(),
			StatTypeExpGained.getInstance(), StatTypeKDR.getInstance(), StatTypeKills.getInstance(),
			StatTypeMobKills.getInstance(), StatTypePlayTime.getInstance()));

	private DefaultStats() {
		try {
			if (Bukkit.getPluginManager().getPlugin("KOTH") != null)
				all.add(StatTypeKothWins.getInstance());
		}
		catch (Exception e) {}

		try {
			if (Bukkit.getPluginManager().getPlugin("BountyHunters") != null)
				all.add(StatTypeHeadDrop.getInstance());
		}
		catch (Exception e) {}

		try {
			if (Bukkit.getPluginManager().getPlugin("EventOrganizer") != null) {
				all.add(StatTypeDeathmatchesWon.getInstance());
				all.add(StatTypeKeyDrops.getInstance());
				all.add(StatTypeSupplyDrops.getInstance());
			}
		}
		catch (Exception e) {}
	}
}
