
package jdz.statsTracker;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import jdz.statsTracker.achievement.AchievementInventories;
import jdz.statsTracker.achievement.AchievementShop;
import jdz.statsTracker.commandHandlers.gca.AchievementCommandExecutor;
import jdz.statsTracker.commandHandlers.gcs.StatsCommandExecutor;
import jdz.statsTracker.config.Config;
import jdz.statsTracker.eventHandlers.*;
import jdz.statsTracker.placeholderHook.PlaceholderHook;
import jdz.statsTracker.stats.StatBuffer;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsDatabase;

public class GCStats extends JavaPlugin {
	public static GCStats plugin;

	@Override
	public void onEnable() {
		plugin = this;

		Config.reloadConfig();

		if (Config.enabledStats.contains(StatType.BLOCKS_MINED))
			StatBuffer.addType(StatType.BLOCKS_MINED);
		if (Config.enabledStats.contains(StatType.BLOCKS_PLACED))
			StatBuffer.addType(StatType.BLOCKS_PLACED);
		
		StatsDatabase.init(this);

		PluginManager pm = Bukkit.getPluginManager();

		if (pm.isPluginEnabled("PlaceholderAPI")) {
			new PlaceholderHook().hook();
		}

		pm.registerEvents(new BlockBreak(), this);
		pm.registerEvents(new BlockPlace(), this);
		pm.registerEvents(new ExpGain(), this);
		pm.registerEvents(new MobDeath(), this);
		pm.registerEvents(new PlayerDeath(), this);
		pm.registerEvents(new LoginLogout(), this);
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

		new AchievementCommandExecutor(this).register();
		new StatsCommandExecutor(this).register();

		for (Player p : Bukkit.getOnlinePlayers())
			LoginLogout.setupPlayer(p);
	}
}
