
package jdz.statsTracker.achievement.achievementTypes;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import jdz.statsTracker.achievement.Achievement;
import jdz.statsTracker.achievement.AchievementManager;

public class TriggeredAchievement extends Achievement {

	public TriggeredAchievement(String name, Material m, short iconDamage, String description) {
		this(name, m, iconDamage, description, 0, "", false);
	}

	public TriggeredAchievement(String name, Material m, short iconDamage, String description, int points,
			String rewardText, boolean hidden) {
		super(name, m, iconDamage, description, points, rewardText, hidden);
	}

	protected void setAchieved(Player player) {
		if (!isAchieved(player))
			AchievementManager.getInstance().setAchieved(player, this);
	}
}
