
package jdz.statsTracker.objective;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import jdz.bukkitUtils.events.Listener;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.stats.NoSaveStatType;

public class ObjectiveManager implements Listener {
	static {
		new Listener() {
			@EventHandler
			public void onPluginUnload(PluginDisableEvent event) {
				unregisterAll(event.getPlugin());
			}
		}.registerEvents(GCStats.getInstance());
	}

	private ObjectiveManager() {}

	private static final Map<Plugin, Set<Objective>> pluginToObjectives = new HashMap<Plugin, Set<Objective>>();

	static Objective registerObjective(Plugin plugin, Objective objective) {
		if (!pluginToObjectives.containsKey(plugin))
			pluginToObjectives.put(plugin, new HashSet<Objective>());
		pluginToObjectives.get(plugin).add(objective);
		if (objective instanceof StatObjective)
			StatObjectiveListener.add((StatObjective)objective);
		return objective;
	}

	static void unregisterObjective(Objective objective) {
		for (Plugin plugin : pluginToObjectives.keySet()) {
			pluginToObjectives.get(plugin).remove(objective);
			if (objective instanceof StatObjective)
				StatObjectiveListener.remove((StatObjective)objective);
		}
	}

	private static void unregisterAll(Plugin plugin) {
		if (pluginToObjectives.containsKey(plugin))
			for (Objective objective : pluginToObjectives.remove(plugin))
				unregisterObjective(objective);
	}
	
	public static void reset(Plugin plugin) {
		if (pluginToObjectives.containsKey(plugin)) {
			Set<NoSaveStatType> types = new HashSet<NoSaveStatType>();
			for (Objective o: pluginToObjectives.get(plugin)) {
				if (o instanceof StatObjective)
					types.add(((StatObjective)o).getStatType());
			}
			for (NoSaveStatType type: types)
				type.resetAll();
		}
		unregisterAll(plugin);
	}
}
