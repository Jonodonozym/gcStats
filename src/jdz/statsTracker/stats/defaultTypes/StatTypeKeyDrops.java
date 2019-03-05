
package jdz.statsTracker.stats.defaultTypes;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.guildcraft.EventOrganizer.events.EventDropPickupEvent;

import jdz.statsTracker.stats.abstractTypes.BufferedStatType;
import lombok.Getter;

public class StatTypeKeyDrops extends BufferedStatType {
	@Getter private static final StatTypeKeyDrops instance = new StatTypeKeyDrops();

	@Override
	public String getName() {
		return "Key drops won";
	}

	@Override
	public String valueToString(double value) {
		return (int) value + "";
	}

	@EventHandler
	public void onEventDropPickup(EventDropPickupEvent e) {
		Player p = e.getPlayer();
		if (!e.getType().equals("keydrop"))
			return;

		set(p, get(p) + 1);
	}
}
