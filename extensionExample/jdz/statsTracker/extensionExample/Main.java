
package jdz.statsTracker.extensionExample;

import org.bukkit.plugin.java.JavaPlugin;

import jdz.statsTracker.achievement.AchievementManager;
import jdz.statsTracker.stats.StatsManager;

public class Main extends JavaPlugin {

	@Override
	public void onEnable() {
		StatsManager.getInstance().addTypes(this, StatTypeJumps.getInstance());
		AchievementManager.getInstance().addAchievements(this, JumpAchievementSeries.achievements);
	}
}
