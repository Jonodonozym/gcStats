
package jdz.statsTracker.objective;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

public interface Objective {
	public String getName();

	public String getDescription();

	public void setRewardText(String text);

	public String getRewardText();

	public void addPlayer(OfflinePlayer player);

	public void removePlayer(OfflinePlayer player);

	public Set<UUID> getPlayers();

	public Set<UUID> getUnlockedPlayers();

	public boolean isUnlocked(OfflinePlayer player);

	public static List<Objective> getObjectives(OfflinePlayer player) {
		return AbstractObjective.getObjectives(player);
	}

	public default Objective register(Plugin plugin) {
		ObjectiveManager.registerObjective(plugin, this);
		return this;
	}

	public default void unRegister() {
		ObjectiveManager.unregisterObjective(this);
	}
}
