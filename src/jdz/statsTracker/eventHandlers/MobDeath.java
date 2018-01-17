
package jdz.statsTracker.eventHandlers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import jdz.statsTracker.GCStatsTrackerConfig;
import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.stats.StatsDatabase;
import jdz.statsTracker.stats.StatType;

public class MobDeath implements Listener{

	@EventHandler(priority=EventPriority.LOWEST)
	public void onMobDeath(EntityDeathEvent e){
		if (GCStatsTrackerConfig.enabledStats.contains(StatType.MOB_KILLS)){
			Player p = e.getEntity().getKiller();
			if (p != null){
				StatsDatabase.getInstance().addStat(p, StatType.MOB_KILLS, 1);
				AchievementData.checkAchievements(p, StatType.MOB_KILLS);
			}
		}
	}
}
