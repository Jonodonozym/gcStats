
package jdz.statsTracker.eventHandlers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.main.Config;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.util.SqlApi;

public class MobDeath implements Listener{

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onMobDeath(EntityDeathEvent e){
		if (Config.statEnabled.get(StatType.MOB_KILLS)){
			Player p = e.getEntity().getKiller();
			if (p != null){
				SqlApi.addStat(Config.dbConnection, p, StatType.MOB_KILLS, 1);
				AchievementData.updateAchievements(p, StatType.MOB_KILLS);
			}
		}
	}
}
