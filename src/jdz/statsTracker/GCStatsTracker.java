
package jdz.statsTracker;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import jdz.statsTracker.achievement.AchievementDatabase;
import jdz.statsTracker.achievement.AchievementInventories;
import jdz.statsTracker.achievement.AchievementShop;
import jdz.statsTracker.commandHandlers.*;
import jdz.statsTracker.eventHandlers.*;
import jdz.statsTracker.placeholderHook.PlaceholderHook;
import jdz.statsTracker.stats.PlayTimeRecorder;
import jdz.statsTracker.stats.StatsDatabase;

public class GCStatsTracker extends JavaPlugin {
	public static GCStatsTracker instance;

	@Override
	public void onEnable() {
		instance = this;

		GCStatsTrackerConfig.reloadConfig();

		PluginManager pm = Bukkit.getPluginManager();

		if (pm.isPluginEnabled("PlaceholderAPI")) {
			new PlaceholderHook().hook();
		}

		pm.registerEvents(StatsDatabase.getInstance(), this);
		pm.registerEvents(AchievementDatabase.getInstance(), this);
		pm.registerEvents(PlayTimeRecorder.getInstance(), this);

		pm.registerEvents(new BlockBreak(), this);
		pm.registerEvents(new BlockPlace(), this);
		pm.registerEvents(new ExpGain(), this);
		pm.registerEvents(new MobDeath(), this);
		pm.registerEvents(new PlayerDeath(), this);
		
		pm.registerEvents(new AchievementInventories(), this);
		pm.registerEvents(new AchievementShop(), this);

		try {
			if (Bukkit.getPluginManager().getPlugin("KOTH") != null)
				pm.registerEvents(new KothWin(), this);
		} catch (Exception e) {
			getLogger().severe("KOTH plugin didn't load correctly or is outdated, skipping");
		}

		try {
			if (Bukkit.getPluginManager().getPlugin("BountyHunter") != null)
				pm.registerEvents(new HeadDrop(), this);
		} catch (Exception e) {
			getLogger().severe("BountyHunter plugin didn't load correctly or is outdated, skipping");
		}

		try {
			if (Bukkit.getPluginManager().getPlugin("EventOrganizer") != null) {
				pm.registerEvents(new Deathmatch(), this);
				pm.registerEvents(new EventDropPickup(), this);
			}
		} catch (Exception e) {
			getLogger().severe("EventOrganizer plugin didn't load correctly or is outdated, skipping");
		}

		getCommand(GCStatsTrackerConfig.achCommand).setExecutor(new AchievementCommands());
		getCommand(GCStatsTrackerConfig.statsCommand).setExecutor(new StatsCommands());

		for (Player p : Bukkit.getOnlinePlayers()){
			StatsDatabase.getInstance().onPlayerJoin(p);
			AchievementDatabase.getInstance().onPlayerJoin(p);
			PlayTimeRecorder.getInstance().onPlayerJoin(p);
		}
	}
	
	@Override
	public void onDisable() {
		for (Player p : Bukkit.getOnlinePlayers()){
			StatsDatabase.getInstance().onPlayerQuit(p);
			AchievementDatabase.getInstance().onPlayerQuit(p);
			PlayTimeRecorder.getInstance().onPlayerQuit(p);
		}
	}
}
