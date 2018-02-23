
package jdz.statsTracker.achievement;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class TriggeredAchievement extends Achievement {

	public TriggeredAchievement(String name, Material m, short iconDamage, String description) {
		this(name, m, iconDamage, description, 0, "", false);
	}

	public TriggeredAchievement(String name, Material m, short iconDamage, String description, int points,
			String rewardText, boolean hidden) {
		super(name, m, iconDamage, description, points, rewardText, hidden);
	}

	protected void setAchieved(Player player) {
		if (!AchievementManager.getInstance().isAchieved(player, this))
			AchievementManager.getInstance().setAchieved(player, this);
	}
}
