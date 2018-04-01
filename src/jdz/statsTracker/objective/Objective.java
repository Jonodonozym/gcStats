
package jdz.statsTracker.objective;

import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

public interface Objective {
	public String getName();
	public String getDescription();
	public String getRewardText();
	public void addPlayer(Player player);
	public void removePlayer(Player player);
	public Set<UUID> getPlayers();
	public Set<UUID> getUnlockedPlayers();
	public boolean isUnlocked(Player player);
	
	public static Set<Objective> getObjectives(Player player){
		return AbstractObjective.getObjectives(player);
	}
	
	
}
