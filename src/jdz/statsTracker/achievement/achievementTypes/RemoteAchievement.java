
package jdz.statsTracker.achievement.achievementTypes;

import org.bukkit.Material;

import jdz.statsTracker.achievement.Achievement;
import lombok.Getter;

public class RemoteAchievement extends Achievement {
	@Getter private final String server;

	public RemoteAchievement(String server, String name, int points, Material m, short iconDamage, String[] description,
			String[] rewardText, boolean hidden) {
		super(name, m, iconDamage, description, points, rewardText, hidden);
		this.server = server;
	}

}
