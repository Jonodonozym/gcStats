
package jdz.statsTracker.eventHandlers;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.main.Config;
import jdz.statsTracker.main.Main;
import jdz.statsTracker.stats.PlayTimeRecorder;
import jdz.statsTracker.stats.StatBuffer;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.util.SqlApi;

import org.bukkit.event.player.PlayerJoinEvent;

public class LoginLogout implements Listener{	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		setupPlayer(e.getPlayer());
	}
	
	public static void setupPlayer(Player p){
		new BukkitRunnable() {
			@Override
			public void run() {
				SqlApi.addPlayer(p);
				PlayTimeRecorder.addPlayer(p);
				AchievementData.addPlayer(p);
				StatBuffer.addPlayer(p);
			}
		}.runTaskAsynchronously(Main.plugin);
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e){
		new BukkitRunnable() {
			@Override
			public void run() {
				Player player = e.getPlayer();
				if (Config.enabledStats.contains(StatType.PLAY_TIME))
				SqlApi.addStat(e.getPlayer(), StatType.PLAY_TIME, 
						(System.currentTimeMillis() - PlayTimeRecorder.lastTime.get(e.getPlayer()))/1000);
				SqlApi.setStat(player, StatType.DIST_WALKED, player.getStatistic(Statistic.WALK_ONE_CM)/100.0);
				PlayTimeRecorder.removePlayer(player);
				StatBuffer.removePlayer(player);
			}
		}.runTaskAsynchronously(Main.plugin);
	}
}
