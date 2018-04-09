
package jdz.statsTracker.objective;

import org.bukkit.entity.Player;

public class TriggeredObjective extends AbstractObjective {
	protected TriggeredObjective(String name, String description) {
		super(name, description);
	}

	@Override
	protected void setUnlocked(Player player) {
		super.setUnlocked(player);
	}
}
