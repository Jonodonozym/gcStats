
package jdz.statsTracker.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import jdz.bukkitUtils.fileIO.FileLogger;
import jdz.bukkitUtils.misc.Config;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.hooks.LeaderHeadsHook;
import jdz.statsTracker.database.StatsDatabase;
import jdz.statsTracker.stats.defaults.DefaultStats;
import lombok.Getter;

public class StatsManager implements Listener {
	@Getter private static final StatsManager instance = new StatsManager();

	private final Set<StatType> enabledStats = new HashSet<StatType>();
	private final List<StatType> enabledStatsList = new ArrayList<StatType>();

	private final Map<Plugin, List<StatType>> pluginToStat = new HashMap<Plugin, List<StatType>>();

	@Getter private Comparator<StatType> comparator = (a, b) -> {
		return a.getName().compareTo(b.getName());
	};

	public Set<StatType> enabledStats() {
		return Collections.unmodifiableSet(enabledStats);
	}

	public List<StatType> enabledStatsSorted() {
		return Collections.unmodifiableList(enabledStatsList);
	}

	public StatType getType(String name) {
		name = name.replaceAll("_", "").replaceAll(" ", "");
		for (StatType statType : enabledStats)
			if (statType.getName().replaceAll(" ", "").equalsIgnoreCase(name))
				return statType;
		return null;
	}

	public List<StatType> getTypes(Plugin plugin) {
		return pluginToStat.get(plugin);
	}

	public void addTypes(Plugin plugin, StatType... statTypes) {
		if (statTypes == null || statTypes.length == 0)
			return;

		if (!pluginToStat.containsKey(plugin))
			pluginToStat.put(plugin, new ArrayList<StatType>());

		ExecutorService es = Executors.newFixedThreadPool(statTypes.length);

		for (StatType statType : statTypes) {
			if (statType == null)
				continue;

			if (enabledStats.contains(statType)) {
				Bukkit.getLogger().warning("Stat type " + statType.getName() + " already registered!");
				continue;
			}

			enabledStats.add(statType);
			enabledStatsList.add(statType);
			pluginToStat.get(plugin).add(statType);

			if (statType instanceof Listener)
				Bukkit.getPluginManager().registerEvents((Listener) statType, GCStats.instance);

			if (!(statType instanceof NoSaveStatType))
				es.execute(() -> {
					StatsDatabase.getInstance().addStatType(statType, true);
					for (Player player : Bukkit.getOnlinePlayers())
						statType.addPlayer(player, StatsDatabase.getInstance().getStat(player, statType));
				});

			if (Bukkit.getPluginManager().isPluginEnabled("LeaderHeads"))
				LeaderHeadsHook.getInstance().addType(statType);
		}

		es.shutdown();
		try {
			es.awaitTermination(1, TimeUnit.MINUTES);
		}
		catch (InterruptedException e) {
			new FileLogger((JavaPlugin) plugin).createErrorLog(e);
		}

		Collections.sort(enabledStatsList, comparator);
	}

	public void removeTypes(StatType... statTypes) {
		if (statTypes == null || statTypes.length == 0)
			return;

		for (StatType statType : statTypes) {
			if (statType == null)
				continue;


			if (!(statType instanceof NoSaveStatType))
				for (Player player : Bukkit.getOnlinePlayers()) {
					Bukkit.getScheduler().runTaskAsynchronously(GCStats.instance, () -> {
						StatsDatabase.getInstance().setStat(player, statType, statType.removePlayer(player));
					});
				}

			if (statType instanceof Listener)
				HandlerList.unregisterAll((Listener) statType);

			if (statType instanceof HookedStatType)
				((HookedStatType) statType).disable();

			enabledStats.remove(statType);
			enabledStatsList.remove(statType);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onUnload(PluginDisableEvent event) {
		if (!pluginToStat.containsKey(event.getPlugin()))
			return;
		List<StatType> types = pluginToStat.remove(event.getPlugin());
		removeTypes(types.toArray(new StatType[types.size()]));
		GCStats.getInstance().getLogger().info(types.size() + " Stat Types unregistered");
	}

	public void loadDefaultStats() {
		Set<StatType> enabledStats = new HashSet<StatType>();

		FileConfiguration config = Config.getConfig(GCStats.instance, "enabledStats.yml");
		for (String key : config.getConfigurationSection("enabledStats").getKeys(false)) {
			if (!config.getBoolean("enabledStats." + key))
				continue;

			for (StatType type : DefaultStats.getInstance().getAll())
				if (type.getName().equalsIgnoreCase(key)) {
					enabledStats.add(type);
					break;
				}
		}

		if (enabledStats.isEmpty())
			return;

		addTypes(GCStats.instance, enabledStats.toArray(new StatType[1]));
	}

	public void setComparator(Comparator<StatType> comparator) {
		this.comparator = comparator;
		Collections.sort(enabledStatsList, comparator);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		for (StatType statType : StatsManager.getInstance().enabledStats())
			if (!(statType instanceof NoSaveStatType))
				Bukkit.getScheduler().runTaskAsynchronously(GCStats.instance, () -> {
					statType.addPlayer(player, StatsDatabase.getInstance().getStat(player, statType));
				});
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		for (StatType statType : StatsManager.getInstance().enabledStats())
			if (statType.get(e.getPlayer()) != statType.getDefault())
				if (!(statType instanceof NoSaveStatType))
					StatsDatabase.getInstance().setStat(e.getPlayer(), statType, statType.removePlayer(e.getPlayer()));
	}
}
