
package jdz.statsTracker;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventException;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import jdz.bukkitUtils.fileIO.FileLogger;
import jdz.statsTracker.achievement.AchievementDatabase;
import jdz.statsTracker.achievement.AchievementInventories;
import jdz.statsTracker.achievement.AchievementManager;
import jdz.statsTracker.achievement.AchievementShop;
import jdz.statsTracker.commandHandlers.*;
import jdz.statsTracker.placeholderHook.PlaceholderHook;
import jdz.statsTracker.stats.StatsDatabase;
import jdz.statsTracker.stats.StatsManager;

public class GCStatsTracker extends JavaPlugin {
	public static GCStatsTracker instance;

	@Override
	public void onEnable() {
		instance = this;

		GCStatsTrackerConfig.reloadConfig();
		
		StatsManager.getInstance().loadDefaultStats();

		AchievementDatabase.getInstance().runOnConnect(() -> {
			GCStatsTrackerConfig.servers = AchievementDatabase.getInstance().getServers();
			AchievementDatabase.getInstance().setServerIcon(GCStatsTrackerConfig.serverName,
					GCStatsTrackerConfig.serverIcon, GCStatsTrackerConfig.serverIconData);

			AchievementManager.getInstance().reloadData();
			AchievementInventories.reload();
			AchievementShop.reload();
		});

		PluginManager pm = Bukkit.getPluginManager();

		if (pm.isPluginEnabled("PlaceholderAPI")) {
			new PlaceholderHook().hook();
		}


		pm.registerEvents(StatsManager.getInstance(), this);
		pm.registerEvents(StatsDatabase.getInstance(), this);
		pm.registerEvents(AchievementManager.getInstance(), this);
		pm.registerEvents(AchievementDatabase.getInstance(), this);
		
		pm.registerEvents(new AchievementInventories(), this);
		pm.registerEvents(new AchievementShop(), this);

		new StatsCommandExecutor(this).register();
		new AchievementCommandExecutor(this).register();

		for (RegisteredListener l : HandlerList.getRegisteredListeners(this))
			try {
				for (Player p : Bukkit.getOnlinePlayers())
					l.callEvent(new PlayerJoinEvent(p, ""));
			} catch (EventException e) {
				new FileLogger(this).createErrorLog(e);
			}
	}

	@Override
	public void onDisable() {
		StatsManager.getInstance().onShutDown();
	}
}