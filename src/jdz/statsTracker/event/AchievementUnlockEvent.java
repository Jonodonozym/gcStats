
package jdz.statsTracker.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import jdz.bukkitUtils.events.Event;
import jdz.statsTracker.achievement.Achievement;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AchievementUnlockEvent extends Event {
	private final Achievement achievement;
	private final Player player;

	public static HandlerList getHandlerList() {
		return getHandlers(StatChangeEvent.class);
	}
}
