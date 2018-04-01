
package jdz.statsTracker.objective;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import jdz.bukkitUtils.events.Listener;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.event.StatChangeEvent;
import jdz.statsTracker.stats.NoSaveStatType;
import jdz.statsTracker.stats.StatType;

public class ObjectiveManager implements Listener {
	static {
		new Listener() {
			@EventHandler
			public void onStatChange(StatChangeEvent event) {
				StatType type = event.getType();
				Player player = event.getPlayer();
				if (typeToObjectives.containsKey(type)) {
					for (Objective objective : typeToObjectives.get(type))
						if (objective.getPlayers().contains(player.getUniqueId()))
							if (!isUnlocked(objective, player) && type.get(player) >= objective.getRequired())
								setUnlocked(objective, player);
				}
			}

			@EventHandler
			public void onPluginUnload(PluginDisableEvent event) {
				if (pluginToObjectives.containsKey(event.getPlugin()))
					for (Objective objective : pluginToObjectives.remove(event.getPlugin()))
						unregisterObjective(objective);
			}
		}.registerEvents(GCStats.getInstance());
	}

	private ObjectiveManager() {}

	private static final Map<NoSaveStatType, Set<Objective>> typeToObjectives = new HashMap<NoSaveStatType, Set<Objective>>();
	private static final Map<Plugin, Set<Objective>> pluginToObjectives = new HashMap<Plugin, Set<Objective>>();

	private static void setUnlocked(Objective objective, Player player) {
		objective.getUnlockedPlayers().add(player.getUniqueId());
	}

	public static boolean isUnlocked(Objective objective, Player player) {
		return objective.getUnlockedPlayers().contains(player.getUniqueId());
	}

	public static Objective newObjective(Plugin plugin, String description, NoSaveStatType statType, double required) {
		return newObjective(plugin, "", description, statType, required);
	}

	public static Objective newObjective(Plugin plugin, String name, String description, NoSaveStatType statType,
			double required) {
		Objective objective = new Objective(name, description, statType, required);

		if (!typeToObjectives.containsKey(statType))
			typeToObjectives.put(statType, new HashSet<Objective>());
		typeToObjectives.get(statType).add(objective);
		if (!pluginToObjectives.containsKey(plugin))
			pluginToObjectives.put(plugin, new HashSet<Objective>());
		pluginToObjectives.get(plugin).add(objective);

		return objective;
	}

	private static void unregisterObjective(Objective o) {
		for (Plugin plugin : pluginToObjectives.keySet())
			pluginToObjectives.get(plugin).remove(o);
		typeToObjectives.get(o.getStatType()).remove(o);
	}
}
