
package jdz.statsTracker.eventHandlers;

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
		if (Config.enabledStats.contains(StatType.KDR)){
			double kills = (int)SqlApi.getStat(killed, StatType.KILLS+"");
			double deaths = (int)SqlApi.getStat(killed, StatType.DEATHS+"");
			if (deaths > 0)
				SqlApi.setStat(killed, StatType.KDR, kills/deaths);
		}
		
		Player killer = e.getEntity().getKiller();
		if (killer != null){
			if (Config.enabledStats.contains(StatType.KILLS)){
				SqlApi.addStat(killer, StatType.KILLS, 1);
				AchievementData.updateAchievements(killer, StatType.KILLS);
			}
			if (Config.enabledStats.contains(StatType.KDR)){
				double kills = (int)SqlApi.getStat(killer, StatType.KILLS+"");
				double deaths = (int)SqlApi.getStat(killer, StatType.DEATHS+"");
				if (deaths > 0){
					SqlApi.setStat(killer, StatType.KDR, kills/deaths);
					AchievementData.updateAchievements(killer, StatType.KDR);
				}
				else{
					SqlApi.setStat(killer, StatType.KDR, kills);
					AchievementData.updateAchievements(killer, StatType.KDR);
				}
			}
		}
	}
}

