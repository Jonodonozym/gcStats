
package jdz.statsTracker.stats.abstractTypes;

import org.bukkit.event.EventHandler;

import jdz.statsTracker.event.StatChangeEvent;
import jdz.statsTracker.stats.StatType;
import lombok.NonNull;

public abstract class MaxStatType extends BufferedStatType {
	@NonNull private final StatType child;

	protected MaxStatType(StatType child) {
		this.child = child;
	}

	@Override
	public String valueToString(double value) {
		return child.valueToString(value);
	}

	@EventHandler
	public void onStatChange(StatChangeEvent event) {
		if (event.getType().equals(child))
			if (event.getNewValue() > get(event.getPlayer()))
				set(event.getPlayer(), event.getNewValue());
	}
}
