
package jdz.statsTracker.eventHandlers;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.main.Config;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.util.SqlApi;

public class PlayerDeath implements Listener{
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e){
		Player killed = e.getEntity();
		if (Config.enabledStats.contains(StatType.DEATHS)){
			SqlApi.addStat(killed, StatType.DEATHS, 1);
			AchievementData.updateAchievements(killed, StatType.DEATHS);
		}
		if (Config.enabledStats.contains(StatType.KDR))
			SqlApi.setStat(killed, StatType.KDR, (double)killed.getStatistic(Statistic.PLAYER_KILLS)/(double)killed.getStatistic(Statistic.DEATHS));
		
		Player killer = e.getEntity().getKiller();
		if (killer != null){
			if (Config.enabledStats.contains(StatType.KILLS)){
				SqlApi.addStat(killer, StatType.KILLS, 1);
				AchievementData.updateAchievements(killer, StatType.KILLS);
			}
			if (Config.enabledStats.contains(StatType.KDR)){
				SqlApi.setStat(killer, StatType.KDR, (double)killer.getStatistic(Statistic.PLAYER_KILLS)/(double)killer.getStatistic(Statistic.DEATHS));
				AchievementData.updateAchievements(killer, StatType.KDR);
			}
		}
	}
}
