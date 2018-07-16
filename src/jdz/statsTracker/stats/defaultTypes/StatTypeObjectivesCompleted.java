
package jdz.statsTracker.stats.defaultTypes;

import org.bukkit.event.EventHandler;

import jdz.statsTracker.event.ObjectiveUnlockEvent;
import jdz.statsTracker.stats.abstractTypes.BufferedStatType;
import lombok.Getter;
import lombok.Setter;

public class StatTypeObjectivesCompleted extends BufferedStatType {
	@Getter private static final StatTypeObjectivesCompleted instance = new StatTypeObjectivesCompleted();
	@Getter @Setter private boolean visible = false;

	private StatTypeObjectivesCompleted() {}


	@Override
	public String getName() {
		return "Objectives";
	}

	@Override
	public String valueToString(double value) {
		return (int) value + "";
	}

	@EventHandler
	public void onObjectiveComplete(ObjectiveUnlockEvent event) {
		add(event.getPlayer(), 1);
	}
}
