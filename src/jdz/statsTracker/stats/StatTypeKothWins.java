
package jdz.statsTracker.stats;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import lombok.Getter;
import subside.plugins.koth.events.KothEndEvent;

class StatTypeKothWins extends BufferedStatType {
	@Getter private static final StatTypeKothWins instance = new StatTypeKothWins();

	@Override
	public String getName() {
		return "KOTH wins";
	}

	@Override
	public String valueToString(double value) {
		return ((int)value)+"";
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onKothEvent(KothEndEvent e){
		Collection<Player> players = e.getWinner().getAvailablePlayers(e.getKoth());
		for (Player p: players)
			set(p, get(p)+1);
	}
}
