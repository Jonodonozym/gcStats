
package jdz.statsTracker.achievement;

import org.bukkit.entity.Player;

import jdz.bukkitUtils.events.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AchievementUnlockEvent extends Event{
	private final Achievement achievement;
	private final Player player;
}
