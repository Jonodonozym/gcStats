
package jdz.statsTracker.stats;

import org.bukkit.entity.Player;

public interface StatType{
	public void addPlayer(Player player, double value);
	public double removePlayer(Player player);
	public double get(Player player);
	public String getName();
	public String valueToString(double value);
	
	public default String getNameUnderscores() {
		return getName().replaceAll(" ", "_");
	}
	
	public default Integer getID() {
		return this.getClass().hashCode();
	}
	
	public default boolean isVisible() {
		return true;
	}
}
