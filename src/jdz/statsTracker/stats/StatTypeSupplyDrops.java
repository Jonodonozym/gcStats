
package jdz.statsTracker.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.guildcraft.EventOrganizer.events.EventDropPickupEvent;

import lombok.Getter;

class StatTypeSupplyDrops extends BufferedStatType{
	@Getter private static final StatTypeSupplyDrops instance = new StatTypeSupplyDrops();

	@Override
	public String getName() {
		return "Supply drops won";
	}

	@Override
	public String valueToString(double value) {
		return ((int)value)+"";
	}
	
	@EventHandler
	public void onEventDropPickup(EventDropPickupEvent e){
		Player p = e.getPlayer();
		
		if (!e.getType().equals("supplydrop") && !e.getType().equals("supplydrops"))
			return;
		
		set(p, get(p)+1);
	}
}
