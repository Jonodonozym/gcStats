
package jdz.statsTracker.stats.defaults;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import jdz.statsTracker.stats.BufferedStatType;
import lombok.Getter;
import me.Indyuce.bh.api.HeadDropEvent;

public class StatTypeHeadDrop extends BufferedStatType {
	@Getter private static final StatTypeHeadDrop instance = new StatTypeHeadDrop();

	@Override
	public String getName() {
		return "Heads removed";
	}

	@Override
	public String valueToString(double value) {
		return ((int) value) + "";
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onHeadDrop(HeadDropEvent e) {
		set(e.getReciever(), get(e.getReciever()) + 1);
	}
}
