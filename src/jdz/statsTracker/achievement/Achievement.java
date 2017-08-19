
package jdz.statsTracker.achievement;

import org.bukkit.Material;

import jdz.statsTracker.stats.StatType;

public class Achievement {
	public final String name;
	public final StatType statType;
	public final double required;
	public final int points;
	public final Material icon;
	public final String description;
	public final String server;
	
	public Achievement(String name, StatType statType, double required, int points, Material m, String description, String server){
		this.name = name;
		this.statType = statType;
		this.required = required;
		this.icon = m;
		this.description = description;
		this.points = points;
		this.server = server;
	}
	
	public boolean isAchieved(double stat){
		return (required >= stat);
	}
}
