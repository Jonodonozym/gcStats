
package jdz.statsTracker.stats.defaults;


import org.bukkit.event.EventHandler;
import org.guildcraft.EventOrganizer.events.deathmatch.DeathmatchWonEvent;

import jdz.statsTracker.stats.BufferedStatType;
import lombok.Getter;

class StatTypeDeathmatchesWon extends BufferedStatType {
	@Getter private static final StatTypeDeathmatchesWon instance = new StatTypeDeathmatchesWon();

	@Override
	public String getName() {
		return "Deathmatch events won";
	}

	@Override
	public String valueToString(double value) {
		return null;
	}

	@EventHandler
	public void onMatchEnd(DeathmatchWonEvent e) {
		set(e.players[0], get(e.players[0])+1);
	}
}
