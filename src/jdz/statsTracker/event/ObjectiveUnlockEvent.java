
package jdz.statsTracker.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import jdz.bukkitUtils.components.events.Event;
import jdz.statsTracker.objective.Objective;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ObjectiveUnlockEvent extends Event {
	private final Objective objective;
	private final Player player;

	public static HandlerList getHandlerList() {
		return getHandlers(ObjectiveUnlockEvent.class);
	}
}
