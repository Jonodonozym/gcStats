
package jdz.statsTracker.stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import jdz.bukkitUtils.fileIO.FileLogger;
import jdz.bukkitUtils.misc.Config;
import jdz.statsTracker.GCStatsTracker;
import lombok.Getter;

public class StatsManager implements Listener {
	@Getter
	private static final StatsManager instance = new StatsManager();

	private final Set<StatType> enabledStats = new HashSet<StatType>();
	private final List<StatType> enabledStatsList = new ArrayList<StatType>();
	
	public Set<StatType> enabledStats() {
		return enabledStats;
	}
	
	public List<StatType> enabledStatsSorted(){
		return enabledStatsList;
	}

	public StatType getType(String name) {
		name = name.replaceAll("_", "").replaceAll(" ", "");
		for (StatType statType : enabledStats)
			if (statType.getName().replaceAll(" ", "").equalsIgnoreCase(name))
				return statType;
		return null;
	}

	public void addTypes(JavaPlugin plugin, StatType... statTypes) {
		ExecutorService es = Executors.newFixedThreadPool(statTypes.length);

		for (StatType statType : statTypes) {
			if (enabledStats.contains(statType))
				continue;

			enabledStats.add(statType);
			enabledStatsList.add(statType);

			if (statType instanceof Listener)
				Bukkit.getPluginManager().registerEvents((Listener) statType, GCStatsTracker.instance);

			es.execute(() -> {
				StatsDatabase.getInstance().addStatType(statType, true);
				for (Player player : Bukkit.getOnlinePlayers())
					statType.addPlayer(player, StatsDatabase.getInstance().getStat(player, statType));
			});
		}

		es.shutdown();
		try {
			es.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			new FileLogger(plugin).createErrorLog(e);
		}
		
		Collections.sort(enabledStatsList, (a,b)->{
			return a.getName().compareTo(b.getName());
		});
	}

	public void removeTypes(StatType... statTypes) {
		for (StatType statType : statTypes) {

			for (Player player : Bukkit.getOnlinePlayers()) {
				Bukkit.getScheduler().runTaskAsynchronously(GCStatsTracker.instance, () -> {
					StatsDatabase.getInstance().setStat(player, statType, statType.removePlayer(player));
				});
			}

			if (statType instanceof Listener)
				HandlerList.unregisterAll((Listener) statType);
			
			if (statType instanceof HookedStatType)
				((HookedStatType)statType).disable();
			
			enabledStats.remove(statType);
			enabledStatsList.remove(statType);
		}
	}

	private final Set<StatType> defaultStats = new HashSet<StatType>(Arrays.asList(StatTypeBlocksMined.getInstance(),
			StatTypeBlocksPlaced.getInstance(), StatTypeDeaths.getInstance(), StatTypeDistanceWalked.getInstance(),
			StatTypeExpGained.getInstance(), StatTypeKDR.getInstance(), StatTypeKills.getInstance(),
			StatTypeMobKills.getInstance(), StatTypePlayTime.getInstance()));

	public void loadDefaultStats() {
		if (!enabledStats.isEmpty())
			throw new RuntimeException("Should not be loading default stats multiple times...");

		try {
			if (Bukkit.getPluginManager().getPlugin("KOTH") != null)
				defaultStats.add(StatTypeKothWins.getInstance());
		} catch (Exception e) {
		}

		try {
			if (Bukkit.getPluginManager().getPlugin("BountyHunter") != null)
				defaultStats.add(StatTypeHeadDrop.getInstance());
		} catch (Exception e) {
		}

		try {
			if (Bukkit.getPluginManager().getPlugin("EventOrganizer") != null) {
				defaultStats.add(StatTypeDeathmatchesWon.getInstance());
				defaultStats.add(StatTypeKeyDrops.getInstance());
				defaultStats.add(StatTypeSupplyDrops.getInstance());
			}
		} catch (Exception e) {
		}

		Set<StatType> enabledStats = new HashSet<StatType>();

		FileConfiguration config = Config.getConfig(GCStatsTracker.instance, "enabledStats.yml");
		for (String key : config.getConfigurationSection("enabledStats").getKeys(false)) {
			if (!config.getBoolean("enabledStats." + key))
				continue;

			for (StatType type : defaultStats)
				if (type.getName().equalsIgnoreCase(key)) {
					enabledStats.add(type);
					break;
				}
		}

		if (enabledStats.isEmpty())
			return;

		addTypes(GCStatsTracker.instance, enabledStats.toArray(new StatType[1]));
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		addPlayer(e.getPlayer());
	}
	
	public void addPlayer(Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				StatsDatabase.getInstance().addPlayer(player);
				for (StatType statType : StatsManager.getInstance().enabledStats())
					statType.addPlayer(player, StatsDatabase.getInstance().getStat(player, statType));
			}
		}.runTaskAsynchronously(GCStatsTracker.instance);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		for (StatType statType : StatsManager.getInstance().enabledStats())
			StatsDatabase.getInstance().setStat(e.getPlayer(), statType, statType.removePlayer(e.getPlayer()));
	}

	public void onShutDown() {
		for (Player player : Bukkit.getOnlinePlayers())
			for (StatType statType : StatsManager.getInstance().enabledStats())
				StatsDatabase.getInstance().setStatSync(player, statType, statType.get(player));
	}
}
