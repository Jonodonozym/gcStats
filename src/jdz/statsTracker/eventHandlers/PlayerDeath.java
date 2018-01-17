
package jdz.statsTracker.eventHandlers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import jdz.statsTracker.GCStatsTracker;
import jdz.statsTracker.GCStatsTrackerConfig;
import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsDatabase;

public class PlayerDeath implements Listener{
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e){
		Player killed = e.getEntity();
		if (GCStatsTrackerConfig.enabledStats.contains(StatType.DEATHS)){
			StatsDatabase.getInstance().addStat(killed, StatType.DEATHS, 1);
			AchievementData.checkAchievements(killed, StatType.DEATHS);
		}
		if (GCStatsTrackerConfig.enabledStats.contains(StatType.KDR)){
			new BukkitRunnable() {
				@Override
				public void run() {
					double kills = (int)StatsDatabase.getInstance().getStat(killed, StatType.KILLS);
					double deaths = (int)StatsDatabase.getInstance().getStat(killed, StatType.DEATHS);
					if (deaths > 0)
						StatsDatabase.getInstance().setStat(killed, StatType.KDR, Math.round(kills/deaths*100)/100.0);
				}
			}.runTaskAsynchronously(GCStatsTracker.instance);
		}
		
		Player killer = e.getEntity().getKiller();
		if (killer != null){
			new BukkitRunnable() {
				@Override
				public void run() {
					if (GCStatsTrackerConfig.enabledStats.contains(StatType.KILLS)) {
						StatsDatabase.getInstance().addStat(killer, StatType.KILLS, 1);
						AchievementData.checkAchievements(killer, StatType.KILLS);
					}
					if (GCStatsTrackerConfig.enabledStats.contains(StatType.KDR)) {
						double kills = (int) StatsDatabase.getInstance().getStat(killer, StatType.KILLS);
						double deaths = (int) StatsDatabase.getInstance().getStat(killer, StatType.DEATHS);
						if (deaths > 0) {
							StatsDatabase.getInstance().setStat(killer, StatType.KDR, Math.round(kills / deaths * 100) / 100.0);
							AchievementData.checkAchievements(killer, StatType.KDR);
						} else {
							StatsDatabase.getInstance().setStat(killer, StatType.KDR, kills);
							AchievementData.checkAchievements(killer, StatType.KDR);
						}
					}
				}
			}.runTaskAsynchronously(GCStatsTracker.instance);
		}
	}
}

