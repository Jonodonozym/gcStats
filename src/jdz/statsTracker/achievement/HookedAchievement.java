
package jdz.statsTracker.achievement;

import org.bukkit.Material;

public abstract class HookedAchievement extends Achievement{

	public HookedAchievement(String name, int points, Material m, short iconDamage, String description) {
		super(name, points, m, iconDamage, description);
	}
}
