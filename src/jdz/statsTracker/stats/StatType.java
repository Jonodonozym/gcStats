
package jdz.statsTracker.stats;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface StatType {
	public boolean hasPlayer(Player player);
	
	public void addPlayer(Player player, double value);

	public double removePlayer(Player player);

	public double get(Player player);

	public String getName();

	public String valueToString(double value);

	public String getNameUnderscores();

	public String getNameNoSpaces();

	public boolean isVisible();

	public double get(OfflinePlayer player);

	public double getDefault();
}
