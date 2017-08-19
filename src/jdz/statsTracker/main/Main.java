
package jdz.statsTracker.main;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import jdz.statsTracker.commandHandlers.AchievementCommands;
import jdz.statsTracker.commandHandlers.StatsCommands;
import jdz.statsTracker.eventHandlers.BlockBreak;
import jdz.statsTracker.eventHandlers.BlockPlace;
import jdz.statsTracker.eventHandlers.ExpGain;
import jdz.statsTracker.eventHandlers.HeadDrop;
import jdz.statsTracker.eventHandlers.KothWin;
import jdz.statsTracker.eventHandlers.LoginLogout;
import jdz.statsTracker.eventHandlers.MobDeath;
import jdz.statsTracker.eventHandlers.PlayerDeath;
import jdz.statsTracker.stats.PlayTimeRecorder;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.util.SqlApi;
import jdz.statsTracker.util.TimedTask;

public class Main extends JavaPlugin{
	public static Main plugin;
	public TimedTask updateDistWalked, updatePlayTime;
	
	@Override
	public void onEnable(){
		plugin = this;
		
		Config.reloadConfig();
		
		updateDistWalked = new TimedTask(Config.autoUpdateDelay, ()-> {
			if (Config.statEnabled.get(StatType.DIST_WALKED))
				for(Player p: Main.plugin.getServer().getOnlinePlayers())
					SqlApi.setStat(Config.dbConnection, p, StatType.DIST_WALKED, p.getStatistic(Statistic.WALK_ONE_CM)/100.0);
		});
		updateDistWalked.start();
		
		updatePlayTime = new PlayTimeRecorder();
		updatePlayTime.start();
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new BlockBreak(), this);
		pm.registerEvents(new BlockPlace(), this);
		pm.registerEvents(new ExpGain(), this);
		pm.registerEvents(new MobDeath(), this);
		pm.registerEvents(new PlayerDeath(), this);
		pm.registerEvents(new LoginLogout(), this);
		if (Bukkit.getPluginManager().getPlugin("KOTH") != null)
			pm.registerEvents(new KothWin(), this);
		if (Bukkit.getPluginManager().getPlugin("BountyHunter") != null)
			pm.registerEvents(new HeadDrop(), this);

		getCommand(Config.achCommand).setExecutor(new AchievementCommands());
		getCommand(Config.statsCommand).setExecutor(new StatsCommands());
	}
	
	@Override
	public void onDisable(){
		SqlApi.close(Config.dbConnection);
	}
}
