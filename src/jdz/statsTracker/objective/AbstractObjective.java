
package jdz.statsTracker.objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import jdz.statsTracker.event.ObjectiveUnlockEvent;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class AbstractObjective implements Objective {
	private static final Map<UUID, List<Objective>> playerToObjective = new HashMap<UUID, List<Objective>>();

	public static List<Objective> getObjectives(OfflinePlayer player) {
		if (!playerToObjective.containsKey(player.getUniqueId()))
			return new ArrayList<Objective>();
		return playerToObjective.get(player.getUniqueId());
	}

	private final String name;
	private final String description;

	private Set<UUID> players = new HashSet<UUID>();
	private Set<UUID> unlockedPlayers = new HashSet<UUID>();

	private String rewardText = "";

	@Override
	public void addPlayer(OfflinePlayer player) {
		if (!playerToObjective.containsKey(player.getUniqueId()))
			playerToObjective.put(player.getUniqueId(), new ArrayList<Objective>());
		playerToObjective.get(player.getUniqueId()).add(this);
		players.add(player.getUniqueId());
	}

	void removeAllPlayers() {
		for (UUID uuid : players)
			if (playerToObjective.containsKey(uuid))
				playerToObjective.get(uuid).remove(this);
		players.clear();
		unlockedPlayers.clear();
	}

	@Override
	public void removePlayer(OfflinePlayer player) {
		if (playerToObjective.containsKey(player.getUniqueId()))
			playerToObjective.get(player.getUniqueId()).remove(this);
		players.remove(player.getUniqueId());
		unlockedPlayers.remove(player.getUniqueId());
	}

	void setUnlocked(Player player) {
		setUnlocked(player, true);
	}

	void setUnlocked(Player player, boolean unlocked) {
		if (unlocked) {
			if (unlockedPlayers.add(player.getUniqueId()))
				new ObjectiveUnlockEvent(this, player).call();
		}
		else
			unlockedPlayers.remove(player.getUniqueId());
	}

	@Override
	public boolean isUnlocked(OfflinePlayer player) {
		return unlockedPlayers.contains(player.getUniqueId());
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof AbstractObjective)
			if (((AbstractObjective) other).getName().equals(name))
				return true;
		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
