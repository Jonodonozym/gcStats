
package jdz.statsTracker.main;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import jdz.statsTracker.achievement.AchievementInventories;
import jdz.statsTracker.achievement.AchievementShop;
import jdz.statsTracker.commandHandlers.*;
import jdz.statsTracker.eventHandlers.*;
import jdz.statsTracker.util.SqlApi;

public class Main extends JavaPlugin{
	public static Main plugin;
	
	@Override
	public void onEnable(){
		plugin = this;
		
		Config.reloadConfig();
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new BlockBreak(), this);
		pm.registerEvents(new BlockPlace(), this);
		pm.registerEvents(new ExpGain(), this);
		pm.registerEvents(new MobDeath(), this);
		pm.registerEvents(new PlayerDeath(), this);
		pm.registerEvents(new LoginLogout(), this);
		pm.registerEvents(new AchievementInventories(), this);
		pm.registerEvents(new AchievementShop(), this);
		if (Bukkit.getPluginManager().getPlugin("KOTH") != null)
			pm.registerEvents(new KothWin(), this);
		if (Bukkit.getPluginManager().getPlugin("BountyHunter") != null)
			pm.registerEvents(new HeadDrop(), this);

		getCommand(Config.achCommand).setExecutor(new AchievementCommands());
		getCommand(Config.statsCommand).setExecutor(new StatsCommands());
		
		for(Player p: Bukkit.getOnlinePlayers())
			LoginLogout.setupPlayer(p);
	}
	
	@Override
	public void onDisable(){
		if (Config.dbConnection != null)
			SqlApi.close(Config.dbConnection);
	}
}
