
package jdz.statsTracker.achievement;

import org.bukkit.Material;

import jdz.statsTracker.stats.StatType;
import lombok.Getter;

public class StatAchievement extends Achievement{
	@Getter private final StatType statType;
	@Getter private final double required;

	public StatAchievement(String name, StatType statType, double required, Material m, short iconDamage,
			String description) {
		super(name, m, iconDamage, description);
		this.statType = statType;
		this.required = required;
	}

}
