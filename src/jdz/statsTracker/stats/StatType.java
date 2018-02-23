
package jdz.statsTracker.stats;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface StatType {
	public void addPlayer(Player player, double value);

	public double removePlayer(Player player);

	public double get(Player player);

	public String getName();

	public String valueToString(double value);

	public default String getNameUnderscores() {
		return getName().replaceAll(" ", "_");
	}
	public default String getNameNoSpaces() {
		return getName().replaceAll(" ", "");
	}

	public default Integer getID() {
		return this.getClass().hashCode();
	}

	public default boolean isVisible() {
		return true;
	}

	public default double get(OfflinePlayer player) {
		if (player.isOnline())
			return get(player.getPlayer());
		else
			return StatsDatabase.getInstance().getStat(player, this);
	}

	public default void updateDatabase(Player player) {
		StatsDatabase.getInstance().setStat(player, this, get(player));
	}

	public default double getDefault() {
		return 0;
	}
}
