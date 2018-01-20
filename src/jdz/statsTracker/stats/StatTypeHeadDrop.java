
package jdz.statsTracker.stats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import lombok.Getter;
import me.Indyuce.bh.ressource.HeadDropEvent;

class StatTypeHeadDrop extends BufferedStatType{
	@Getter private static final StatTypeHeadDrop instance = new StatTypeHeadDrop();

	@Override
	public String getName() {
		return "Heads removed";
	}

	@Override
	public String valueToString(double value) {
		return ((int)value)+"";
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onHeadDrop(HeadDropEvent e) {
		set(e.getReciever(), get(e.getReciever())+1);
	}
}
