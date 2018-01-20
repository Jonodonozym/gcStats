
package jdz.statsTracker.achievement;

import org.bukkit.Material;

import lombok.Getter;

class RemoteAchievement extends Achievement{
	@Getter private final String server;

	public RemoteAchievement(String name, int points, Material m, short iconDamage, String description, String server) {
		super(name, points, m, iconDamage, description);
		this.server = server;
	}

}
