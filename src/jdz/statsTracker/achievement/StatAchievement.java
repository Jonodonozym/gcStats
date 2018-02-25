
package jdz.statsTracker.achievement;

import org.bukkit.Material;

import jdz.statsTracker.stats.StatType;
import lombok.Getter;
import lombok.NonNull;

public class StatAchievement extends Achievement {
	@NonNull @Getter private final StatType statType;
	@Getter private final double required;

	public StatAchievement(String name, StatType type, double required, Material m, short iconDamage,
			String description) {
		this(name, type, required, m, iconDamage, description, 0, "", false);
	}

	public StatAchievement(String name, StatType statType, double required, Material m, short iconDamage,
			String description, int points, String rewardText, boolean hidden) {
		super(name, m, iconDamage, description, points, rewardText, hidden);
		this.statType = statType;
		this.required = required;
	}
}
