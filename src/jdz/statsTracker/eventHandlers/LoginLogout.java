
package jdz.statsTracker.eventHandlers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.main.Config;
import jdz.statsTracker.stats.PlayTimeRecorder;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.util.SqlApi;

import org.bukkit.event.player.PlayerJoinEvent;

public class LoginLogout implements Listener{	
	@EventHandler(ignoreCancelled=true, priority=EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e){
		setupPlayer(e.getPlayer());
	}
	
	public static void setupPlayer(Player p){
		SqlApi.addPlayer(p);
		PlayTimeRecorder.addPlayer(p);
		AchievementData.addPlayer(p);
	}

	@EventHandler(ignoreCancelled=true, priority=EventPriority.LOWEST)
	public void onPlayerLeave(PlayerQuitEvent e){
		if (Config.enabledStats.contains(StatType.PLAY_TIME))
		SqlApi.addStat(e.getPlayer(), StatType.PLAY_TIME, 
				(System.currentTimeMillis() - PlayTimeRecorder.lastTime.get(e.getPlayer()))/1000);
		PlayTimeRecorder.removePlayer(e.getPlayer());
	}
}
