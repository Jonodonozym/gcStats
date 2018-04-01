
package jdz.statsTracker.objective;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class AbstractObjective implements Objective{
	protected static final Map<UUID, Set<Objective>> playerToObjective = new HashMap<UUID, Set<Objective>>();

	public static Set<Objective> getObjectives(Player player) {
		if (!playerToObjective.containsKey(player.getUniqueId()))
			return new HashSet<Objective>();
		return playerToObjective.get(player.getUniqueId());
	}

	private final String name;
	private final String description;

	private Set<UUID> players = new HashSet<UUID>();
	private Set<UUID> unlockedPlayers = new HashSet<UUID>();

	private String rewardText = "";

	public void addPlayer(Player player) {
		if (!playerToObjective.containsKey(player.getUniqueId()))
			playerToObjective.put(player.getUniqueId(), new HashSet<Objective>());
		players.add(player.getUniqueId());
	}

	public void removePlayer(Player player) {
		players.remove(player.getUniqueId());
		if (playerToObjective.containsKey(player.getUniqueId()))
			playerToObjective.get(player.getUniqueId()).remove(this);
		unlockedPlayers.remove(player.getUniqueId());
	}
	
	void setUnlocked(Player player) {
		setUnlocked(player, true);
	}
	
	void setUnlocked(Player player, boolean unlocked) {
		if (unlocked)
			unlockedPlayers.add(player.getUniqueId());
		else
			unlockedPlayers.remove(player.getUniqueId());
	}

	public boolean isUnlocked(Player player) {
		return unlockedPlayers.contains(player.getUniqueId());
	}
}
