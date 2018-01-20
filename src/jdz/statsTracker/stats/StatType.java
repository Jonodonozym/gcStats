
package jdz.statsTracker.stats;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public interface StatType{
	static final Map<Class<? extends StatType>, Integer> classToID = new HashMap<Class<? extends StatType>, Integer>();

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
}
