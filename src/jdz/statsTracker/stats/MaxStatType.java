
package jdz.statsTracker.stats;

import org.bukkit.event.EventHandler;

import jdz.statsTracker.event.StatChangeEvent;

public abstract class MaxStatType extends BufferedStatType {
	private final StatType child;

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
