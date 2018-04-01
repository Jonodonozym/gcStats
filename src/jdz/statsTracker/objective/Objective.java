
package jdz.statsTracker.objective;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import jdz.statsTracker.stats.NoSaveStatType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Objective {
	private static final Map<UUID, Set<Objective>> playerToObjective = new HashMap<UUID, Set<Objective>>();

	public static Set<Objective> getObjectives(Player player) {
		if (!playerToObjective.containsKey(player.getUniqueId()))
			return new HashSet<Objective>();
		return playerToObjective.get(player.getUniqueId());
	}


	public static Objective newObjective(Plugin plugin, String description, NoSaveStatType statType, double required) {
		return ObjectiveManager.newObjective(plugin, description, statType, required);
	}

	public static Objective newObjective(Plugin plugin, String name, String description, NoSaveStatType statType,
			double required) {
		return ObjectiveManager.newObjective(plugin, name, description, statType, required);
	}

	private final String name;
	private final String description;
	private final NoSaveStatType statType;
	private final double required;

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
	}

	public boolean isUnlocked(Player player) {
		return ObjectiveManager.isUnlocked(this, player);
	}
}
