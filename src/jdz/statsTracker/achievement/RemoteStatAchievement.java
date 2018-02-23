
package jdz.statsTracker.achievement;

import org.bukkit.Material;

import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsManager;
import lombok.Getter;

class RemoteStatAchievement extends RemoteAchievement {
	@Getter private final String statTypeName;
	@Getter private final StatType statType;
	@Getter private final double requirement;

	public RemoteStatAchievement(String server, String name, int points, Material m, short iconDamage,
			String description, String rewardText, boolean hidden, String statType, double requirement) {
		super(server, name, points, m, iconDamage, description, rewardText, hidden);
		this.statTypeName = statType;
		this.requirement = requirement;
		this.statType = StatsManager.getInstance().getType(statType);
	}

}
