
package jdz.statsTracker.eventHandlers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.guildcraft.EventOrganizer.events.deathmatch.DeathmatchWonEvent;

import jdz.statsTracker.stats.StatsDatabase;
import jdz.statsTracker.GCStatsTrackerConfig;
import jdz.statsTracker.stats.StatType;

public class Deathmatch implements Listener {

	@EventHandler
	public void onMatchEnd(DeathmatchWonEvent e){
		if (GCStatsTrackerConfig.enabledStats.contains(StatType.DEATHMATCH_EVENTS_WON)){
			Player winner = e.players[0];
			StatsDatabase.getInstance().addStat(winner, StatType.DEATHMATCH_EVENTS_WON, 1);
		}
	}
}
