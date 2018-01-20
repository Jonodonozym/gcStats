
package jdz.statsTracker.extensionExample;

import org.bukkit.Material;

import jdz.bukkitUtils.misc.RomanNumber;
import jdz.statsTracker.achievement.Achievement;
import jdz.statsTracker.achievement.StatAchievement;

public class JumpAchievementSeries {
	public static Achievement[] achievements = new Achievement[4];
	static {
		for (int i = 0; i < 4; i++)
			achievements[i] = new StatAchievement("Rabbit "+RomanNumber.of(i+1), StatTypeJumps.getInstance(), Math.pow(10, i + 1), i + 1,
					Material.RABBIT_FOOT, (short)0, "Jump like a rabbit, sting like a rabbit");
	}
}
