
package jdz.statsTracker.extensionExample;

import org.bukkit.Material;

import jdz.bukkitUtils.components.RomanNumber;
import jdz.statsTracker.achievement.Achievement;
import jdz.statsTracker.achievement.achievementTypes.StatAchievement;

public class JumpAchievementSeries {
	public static Achievement[] achievements = new Achievement[4];
	static {
		for (int i = 0; i < 4; i++) {
			achievements[i] = new StatAchievement("Rabbit " + RomanNumber.of(i + 1), StatTypeJumps.getInstance(),
					Math.pow(10, i + 1), Material.RABBIT_FOOT, (short) 0,
					"Jump like a rabbit... uhh... sting like a rabbit?", i + 1, "", false);
		}
	}
}
