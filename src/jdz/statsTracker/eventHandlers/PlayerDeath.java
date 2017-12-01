
package jdz.statsTracker.eventHandlers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.config.Config;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsDatabase;

public class PlayerDeath implements Listener{
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e){
		Player killed = e.getEntity();
		if (Config.enabledStats.contains(StatType.DEATHS)){
			StatsDatabase.getInstance().addStat(killed, StatType.DEATHS, 1);
			AchievementData.updateAchievements(killed, StatType.DEATHS);
		}
		if (Config.enabledStats.contains(StatType.KDR)){
			double kills = (int)StatsDatabase.getInstance().getStat(killed, StatType.KILLS+"");
			double deaths = (int)StatsDatabase.getInstance().getStat(killed, StatType.DEATHS+"");
			if (deaths > 0)
				StatsDatabase.getInstance().setStat(killed, StatType.KDR, Math.round(kills/deaths*100)/100.0);
		}
		
		Player killer = e.getEntity().getKiller();
		if (killer != null){
			if (Config.enabledStats.contains(StatType.KILLS)){
				StatsDatabase.getInstance().addStat(killer, StatType.KILLS, 1);
				AchievementData.updateAchievements(killer, StatType.KILLS);
			}
			if (Config.enabledStats.contains(StatType.KDR)){
				double kills = (int)StatsDatabase.getInstance().getStat(killer, StatType.KILLS+"");
				double deaths = (int)StatsDatabase.getInstance().getStat(killer, StatType.DEATHS+"");
				if (deaths > 0){
					StatsDatabase.getInstance().setStat(killer, StatType.KDR, Math.round(kills/deaths*100)/100.0);
					AchievementData.updateAchievements(killer, StatType.KDR);
				}
				else{
					StatsDatabase.getInstance().setStat(killer, StatType.KDR, kills);
					AchievementData.updateAchievements(killer, StatType.KDR);
				}
			}
		}
	}
}

