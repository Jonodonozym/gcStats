
package jdz.statsTracker.event;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import jdz.bukkitUtils.components.events.Cancellable;
import jdz.bukkitUtils.components.events.Event;
import jdz.statsTracker.stats.StatType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StatChangeEvent extends Event implements Cancellable {
	@Getter private final Player player;
	@Getter private final UUID uuid;
	@Getter private final StatType type;
	@Getter private final double oldValue, newValue;

	public static HandlerList getHandlerList() {
		return getHandlers(StatChangeEvent.class);
	}
}
