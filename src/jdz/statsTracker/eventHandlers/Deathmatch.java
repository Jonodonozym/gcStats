
package jdz.statsTracker.eventHandlers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.guildcraft.EventOrganizer.events.deathmatch.DeathmatchWonEvent;

import jdz.statsTracker.main.Config;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.util.SqlApi;

public class Deathmatch implements Listener {

	@EventHandler
	public void onMatchEnd(DeathmatchWonEvent e){
		if (Config.enabledStats.contains(StatType.DEATHMATCH_EVENTS_WON)){
			Player winner = e.players[0];
			SqlApi.addStat(winner, StatType.DEATHMATCH_EVENTS_WON, 1);
		}
	}
}
