
package jdz.statsTracker.extensionExample;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import jdz.statsTracker.stats.HookedStatType;

public class StatTypeJumps extends HookedStatType{
	private static final StatTypeJumps instance = new StatTypeJumps();
	public static StatTypeJumps getInstance() { return instance; }

	@Override
	public double get(Player player) {
		return player.getStatistic(Statistic.JUMP);
	}

	@Override
	public String getName() {
		return "Jumps";
	}

	@Override
	public String valueToString(double value) {
		return ((int)value)+"";
	}

}
