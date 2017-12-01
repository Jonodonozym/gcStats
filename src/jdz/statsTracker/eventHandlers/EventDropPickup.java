
package jdz.statsTracker.eventHandlers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.guildcraft.EventOrganizer.events.EventDropPickupEvent;

import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsDatabase;

public class EventDropPickup implements Listener{

	@EventHandler
	public void onEventDropPickup(EventDropPickupEvent e){
		Player p = e.getPlayer();
		switch(e.getType()){
		case "keydrop":
			StatsDatabase.getInstance().addStat(p, StatType.KEY_DROPS_WON, 1);
			break;
		case "supplydrop":
		case "supplydrops":
			StatsDatabase.getInstance().addStat(p, StatType.SUPPLY_DROPS_WON, 1);
			break;
		}
	}
}
