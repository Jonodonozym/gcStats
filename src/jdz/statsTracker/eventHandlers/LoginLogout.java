
package jdz.statsTracker.eventHandlers;

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
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent e){
		PlayTimeRecorder.lastTime.put(e.getPlayer(), System.currentTimeMillis());
		AchievementData.addPlayer(e.getPlayer());
		SqlApi.addPlayer(Config.dbConnection, e.getPlayer());
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerLeave(PlayerQuitEvent e){
		SqlApi.addStat(Config.dbConnection, e.getPlayer(), StatType.PLAY_TIME_SECONDS, 
				(System.currentTimeMillis() - PlayTimeRecorder.lastTime.get(e.getPlayer()))/1000);
		PlayTimeRecorder.lastTime.remove(e.getPlayer());
		AchievementData.removePlayer(e.getPlayer());
	}
}
