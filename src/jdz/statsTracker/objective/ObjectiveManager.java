
package jdz.statsTracker.objective;

import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;
import static org.bukkit.ChatColor.RESET;
import static org.bukkit.ChatColor.WHITE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import jdz.bukkitUtils.events.Listener;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.stats.NoSaveStatType;
import jdz.statsTracker.stats.StatType;
import lombok.Setter;

public class ObjectiveManager implements Listener {
	static {
		new Listener() {
			@EventHandler
			public void onPluginUnload(PluginDisableEvent event) {
				if (event.getPlugin().equals(GCStats.getInstance()))
					return;
				
				unregisterAll(event.getPlugin());
			}
		}.registerEvents(GCStats.getInstance());
	}

	private ObjectiveManager() {}

	private static final Map<Plugin, Set<Objective>> pluginToObjectives = new HashMap<Plugin, Set<Objective>>();

	@Setter private static boolean unlockedObjectivesShown = true;
	@Setter private static boolean objectiveNamesShown = true;

	static Objective registerObjective(Plugin plugin, Objective objective) {
		if (!pluginToObjectives.containsKey(plugin))
			pluginToObjectives.put(plugin, new HashSet<Objective>());

		if (pluginToObjectives.get(plugin).contains(objective)) {
			System.out.println("Objective with name " + objective.getName() + " already registered!");
			for (Objective o : pluginToObjectives.get(plugin))
				if (o.equals(objective))
					return o;
			return objective;
		}

		pluginToObjectives.get(plugin).add(objective);
		if (objective instanceof StatObjective)
			StatObjectiveListener.add((StatObjective) objective);
		return objective;
	}

	static void unregisterObjective(Objective objective) {
		for (Plugin plugin : pluginToObjectives.keySet())
			pluginToObjectives.get(plugin).remove(objective);

		((AbstractObjective)objective).removeAllPlayers();
		
		if (objective instanceof StatObjective)
			StatObjectiveListener.remove((StatObjective) objective);
	}

	private static void unregisterAll(Plugin plugin) {
		if (pluginToObjectives.containsKey(plugin))
			for (Objective objective : pluginToObjectives.remove(plugin))
				unregisterObjective(objective);
	}

	public static void reset(Plugin plugin) {
		if (pluginToObjectives.containsKey(plugin)) {
			Set<NoSaveStatType> types = new HashSet<NoSaveStatType>();
			for (Objective o : pluginToObjectives.get(plugin))
				if (o instanceof StatObjective)
					types.add(((StatObjective) o).getStatType());
			for (NoSaveStatType type : types)
				type.resetAll();
		}
		unregisterAll(plugin);
	}

	public static void displayObjectives(Player player) {
		List<Objective> objectives = Objective.getObjectives(player);
		List<Objective> lockedObjectives = new ArrayList<Objective>(objectives);
		for (Objective o : Objective.getObjectives(player))
			if (o.isUnlocked(player))
				lockedObjectives.remove(o);

		if (!unlockedObjectivesShown && lockedObjectives.isEmpty() || objectives.isEmpty()) {
			player.sendMessage(AQUA + "You don't have any more objectives to complete");
			return;
		}

		int completed = objectives.size() - lockedObjectives.size();

		player.sendMessage(GREEN + "You have completed " + completed + " / " + objectives.size() + " objectives: ");
		displayObjectives(player, unlockedObjectivesShown ? objectives : lockedObjectives);
	}

	public static void displayObjectives(Player player, List<Objective> objectives) {
		for (Objective objective : objectives) {
			String message = "    - " + (objective.isUnlocked(player) ? GREEN : RED);
			if (objectiveNamesShown)
				message += objective.getName() + ": ";
			message += objective.getDescription();

			if (!objective.getRewardText().equals(""))
				message += AQUA + "" + RESET + " (" + objective.getRewardText() + AQUA + RESET + ")";

			if (objective instanceof StatObjective && !objective.isUnlocked(player)) {
				StatObjective statObjective = (StatObjective) objective;
				StatType type = statObjective.getStatType();
				message += WHITE + " " + type.valueToString(type.get(player)) + " / "
						+ type.valueToString(statObjective.getRequired());
			}

			player.sendMessage(message);
		}
	}
}
