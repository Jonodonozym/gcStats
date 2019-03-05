
package jdz.statsTracker.stats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import jdz.bukkitUtils.configuration.Config;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.hooks.LeaderHeadsHook;
import jdz.statsTracker.stats.abstractTypes.BufferedStatType;
import jdz.statsTracker.stats.abstractTypes.HookedStatType;
import jdz.statsTracker.stats.abstractTypes.NoSaveStatType;
import jdz.statsTracker.stats.defaultTypes.DefaultStats;
import lombok.Getter;
import lombok.Setter;

public class StatsManager implements Listener {
	@Getter private static final StatsManager instance = new StatsManager();

	private final Set<StatType> enabledStats = new HashSet<>();
	private final List<StatType> enabledStatsList = new ArrayList<>();
	private final Map<Plugin, List<StatType>> pluginToStat = new HashMap<>();
	@Setter private Collection<BufferedStatType> updateDatabaseTypes = null;
	@Getter private Comparator<StatType> comparator = (a, b) -> {
		return a.getName().compareTo(b.getName());
	};

	public Set<StatType> enabledStats() {
		return Collections.unmodifiableSet(enabledStats);
	}

	public List<StatType> enabledStatsSorted() {
		return Collections.unmodifiableList(enabledStatsList);
	}

	public List<StatType> enabledStats(Plugin plugin) {
		if (!pluginToStat.containsKey(plugin))
			return Collections.unmodifiableList(new ArrayList<StatType>());
		return Collections.unmodifiableList(pluginToStat.get(plugin));
	}

	public StatType getType(String name) {
		for (StatType statType : enabledStats)
			if (statType.getNameNoSpaces().equalsIgnoreCase(name)
					|| statType.getNameUnderscores().equalsIgnoreCase(name))
				return statType;
		return null;
	}

	public BufferedStatType getBufferedType(String name) {
		for (BufferedStatType statType : getBufferedTypes())
			if (statType.getNameNoSpaces().equalsIgnoreCase(name)
					|| statType.getNameUnderscores().equalsIgnoreCase(name))
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
				Bukkit.getPluginManager().registerEvents((Listener) statType, GCStats.getInstance());

			LeaderHeadsHook.getInstance().addType(statType);

			if (!(statType instanceof NoSaveStatType))
				StatsDatabase.getInstance().addStatType(statType, true);
		}

		Collections.sort(enabledStatsList, comparator);

		if (Bukkit.getOnlinePlayers().size() == 0)
			return;

		final List<StatType> typesList = new ArrayList<>(Arrays.asList(statTypes));
		typesList.removeIf((type) -> {
			if (type == null)
				return true;
			if (type instanceof NoSaveStatType) {
				for (Player player : Bukkit.getOnlinePlayers())
					type.addPlayer(player, type.getDefault());
				return true;
			}
			return false;
		});

		if (typesList.isEmpty())
			return;

		StatsDatabaseSQL.getInstance().runOnConnect(() -> {
			Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
				for (Player player : Bukkit.getOnlinePlayers()) {
					Map<StatType, Double> stats = StatsDatabase.getInstance().getStats(player, typesList);
					for (StatType type : stats.keySet())
						type.addPlayer(player, stats.get(type));
				}
			});
		});
	}

	public void removeTypes(StatType... statTypes) {
		if (statTypes == null || statTypes.length == 0)
			return;

		for (StatType statType : statTypes) {
			if (statType == null)
				continue;


			if (!(statType instanceof NoSaveStatType))
				for (Player player : Bukkit.getOnlinePlayers())
					Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
						StatsDatabase.getInstance().setStat(player, statType, statType.removePlayer(player));
					});

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
		if (event.getPlugin().equals(GCStats.getInstance()))
			return;

		if (!pluginToStat.containsKey(event.getPlugin()))
			return;
		List<StatType> types = pluginToStat.remove(event.getPlugin());
		removeTypes(types.toArray(new StatType[types.size()]));
		GCStats.getInstance().getLogger().info(types.size() + " Stat Types unregistered");
	}

	public void loadDefaultStats() {
		Set<StatType> enabledStats = new HashSet<>();

		FileConfiguration config = Config.getConfig(GCStats.getInstance(), "enabledStats.yml");
		for (String key : config.getConfigurationSection("enabledStats").getKeys(false)) {
			if (!config.getBoolean("enabledStats." + key)) {
				config.set("enabledStats." + key, false);
				continue;
			}

			for (StatType type : DefaultStats.getInstance().getAll())
				if (type.getName().equalsIgnoreCase(key)) {
					enabledStats.add(type);
					break;
				}
		}

		try {
			config.save(Config.getConfigFile(GCStats.getInstance(), "enabledStats.yml"));
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		if (enabledStats.isEmpty())
			return;

		addTypes(GCStats.getInstance(), enabledStats.toArray(new StatType[1]));
	}

	public void setComparator(Comparator<StatType> comparator) {
		this.comparator = comparator;
		Collections.sort(enabledStatsList, comparator);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();

		for (StatType statType : StatsManager.getInstance().enabledStats())
			statType.addPlayer(player, statType.getDefault());

		Set<BufferedStatType> types = getBufferedTypes();
		types.removeIf((type) -> {
			return type instanceof NoSaveStatType;
		});
		if (types.isEmpty())
			return;

		Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
			StatsDatabaseSQL.getInstance().runOnConnect(() -> {
				if (!StatsDatabase.getInstance().hasPlayer(player)) {
					for (StatType type : types)
						type.addPlayer(player, type.getDefault());

					StatsDatabase.getInstance().addPlayer(player);
					return;
				}

				Map<StatType, Double> statToValue = StatsDatabase.getInstance().getStats(player, types);
				for (BufferedStatType type : types) {
					type.addPlayer(player, statToValue.get(type));
					type.setHasFetched(player, true);
				}
			});
		});
	}

	public Set<StatType> getVisibleTypes() {
		Set<StatType> visibleTypes = new HashSet<>();
		for (StatType type : enabledStats)
			if (type.isVisible())
				visibleTypes.add(type);
		return visibleTypes;
	}

	public Set<BufferedStatType> getBufferedTypes() {
		Set<BufferedStatType> bufferedTypes = new HashSet<>();
		for (StatType type : enabledStats)
			if (type instanceof BufferedStatType)
				bufferedTypes.add((BufferedStatType) type);
		return bufferedTypes;
	}

	private Set<StatType> getSaveableTypes() {
		Set<StatType> types = new HashSet<>(enabledStats());
		types.removeIf((t) -> {
			return t instanceof NoSaveStatType;
		});
		return types;
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		updateDatabase(e.getPlayer(), updateDatabaseTypes == null ? getSaveableTypes() : updateDatabaseTypes);
		for (BufferedStatType type : getBufferedTypes()) {
			type.removePlayer(e.getPlayer());
			type.setHasFetched(e.getPlayer(), false);
		}
	}

	public void updateDatabase(Player player, StatType type) {
		updateDatabase(player, Arrays.asList(type));
	}

	public void updateDatabase(Player player, Collection<? extends StatType> typesToSave) {
		Map<StatType, Double> typeToValue = new HashMap<>();

		for (StatType type : typesToSave) {
			if (type instanceof BufferedStatType)
				if (!((BufferedStatType) type).hasFetched(player) || type.get(player) == type.getDefault())
					continue;
			typeToValue.put(type, type.get(player));
		}

		if (typeToValue.isEmpty())
			return;

		StatsDatabase.getInstance().setStats(player, typeToValue);
	}
}
