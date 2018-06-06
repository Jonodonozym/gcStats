
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

import jdz.bukkitUtils.fileIO.FileLogger;
import jdz.bukkitUtils.misc.Config;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.hooks.LeaderHeadsHook;
import jdz.statsTracker.stats.abstractTypes.BufferedStatType;
import jdz.statsTracker.stats.abstractTypes.HookedStatType;
import jdz.statsTracker.stats.abstractTypes.NoSaveStatType;
import jdz.statsTracker.stats.StatsDatabase;
import jdz.statsTracker.stats.defaultTypes.DefaultStats;
import lombok.Getter;
import lombok.Setter;

public class StatsManager implements Listener {
	@Getter private static final StatsManager instance = new StatsManager();

	private final Set<StatType> enabledStats = new HashSet<StatType>();
	private final List<StatType> enabledStatsList = new ArrayList<StatType>();

	private final Map<Plugin, List<StatType>> pluginToStat = new HashMap<Plugin, List<StatType>>();

	@Setter private boolean saveOnQuit = true;
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
				Bukkit.getPluginManager().registerEvents((Listener) statType, GCStats.getInstance());

			if ((statType instanceof NoSaveStatType))
				for (Player player : Bukkit.getOnlinePlayers())
					statType.addPlayer(player, statType.getDefault());
			else
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
			new FileLogger(plugin).createErrorLog(e);
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
					Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
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
		if (event.getPlugin().equals(GCStats.getInstance()))
			return;

		if (!pluginToStat.containsKey(event.getPlugin()))
			return;
		List<StatType> types = pluginToStat.remove(event.getPlugin());
		removeTypes(types.toArray(new StatType[types.size()]));
		GCStats.getInstance().getLogger().info(types.size() + " Stat Types unregistered");
	}

	public void loadDefaultStats() {
		Set<StatType> enabledStats = new HashSet<StatType>();

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

	public Set<BufferedStatType> getBufferedTypes() {
		Set<BufferedStatType> bufferedTypes = new HashSet<BufferedStatType>();
		for (StatType type : enabledStats)
			if (type instanceof BufferedStatType)
				bufferedTypes.add((BufferedStatType) type);
		return bufferedTypes;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
			for (StatType statType : StatsManager.getInstance().enabledStats())
				statType.addPlayer(player, statType.getDefault());

			Set<BufferedStatType> types = getBufferedTypes();
			if (types.isEmpty())
				return;

			StatsDatabaseSQL.getInstance().runOnConnect(() -> {
				if (!StatsDatabase.getInstance().hasPlayer(player)) {
					StatsDatabase.getInstance().addPlayerSync(player);
					return;
				}

				ExecutorService es = Executors.newFixedThreadPool(types.size());
				for (BufferedStatType statType : types)
					es.execute(() -> {
						double amount = StatsDatabase.getInstance().getStat(player, statType);
						statType.set(player.getUniqueId(), amount);
						statType.setHasFetched(player, true);
					});
				es.shutdown();
				try {
					es.awaitTermination(60, TimeUnit.SECONDS);
					GCStats.getInstance().getLogger().info("Stats fetched for " + player.getName());
				}
				catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			});

		});
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		if (saveOnQuit)
			Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
				Collection<BufferedStatType> types = getSaveableTypes(e.getPlayer());
				updateDatabaseSync(e.getPlayer(), types);
				for (BufferedStatType type : getBufferedTypes()) {
					type.removePlayer(e.getPlayer());
					type.setHasFetched(e.getPlayer(), false);
				}
			});
	}

	private Collection<BufferedStatType> getSaveableTypes(Player player) {
		Collection<BufferedStatType> typesToSave = updateDatabaseTypes == null ? getBufferedTypes()
				: updateDatabaseTypes;
		typesToSave = new HashSet<BufferedStatType>(typesToSave);
		typesToSave.removeIf((type) -> {
			return type.get(player) == type.getDefault() || !type.hasFetched(player);
		});
		return typesToSave;
	}

	public void updateDatabase(Player player, BufferedStatType type) {
		updateDatabase(player, Arrays.asList(type));
	}

	public void updateDatabase(Player player, Collection<BufferedStatType> typesToSave) {
		Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
			updateDatabaseSync(player, typesToSave);
		});
	}

	public void updateDatabaseSync(Player player, BufferedStatType type) {
		updateDatabaseSync(player, Arrays.asList(type));
	}

	public void updateDatabaseSync(Player player, Collection<BufferedStatType> typesToSave) {
		Map<StatType, Double> typeToValue = new HashMap<StatType, Double>();
		for (BufferedStatType type : typesToSave)
			if (type.hasFetched(player))
				typeToValue.put(type, type.get(player));
		
		if (typeToValue.isEmpty())
			return;
		
		StatsDatabase.getInstance().setStatsSync(player, typeToValue);
		GCStats.getInstance().getLogger().info("Stats saved for " + player.getName());
	}
}
