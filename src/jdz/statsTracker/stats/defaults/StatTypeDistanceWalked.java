
package jdz.statsTracker.stats.defaults;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import jdz.statsTracker.stats.HookedStatType;
import lombok.Getter;

public class StatTypeDistanceWalked extends HookedStatType {
	@Getter private static final StatTypeDistanceWalked instance = new StatTypeDistanceWalked();

	@Override
	public String getName() {
		return "Distance walked";
	}

	@Override
	public String valueToString(double value) {
		return distanceFromMeters((int) value);
	}

	private String distanceFromMeters(int meters) {
		double km = meters / 1000.0;
		if (km > 0)
			return km + "km";
		return meters + "m";
	}

	@Override
	public double get(Player player) {
		return player.getStatistic(Statistic.WALK_ONE_CM) / 100 + player.getStatistic(Statistic.SPRINT_ONE_CM) / 100;
	}
}
