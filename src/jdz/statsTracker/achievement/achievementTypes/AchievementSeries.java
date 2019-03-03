
package jdz.statsTracker.achievement.achievementTypes;

import java.util.List;

import org.bukkit.plugin.Plugin;

import jdz.statsTracker.achievement.Achievement;
import lombok.AllArgsConstructor;

@AllArgsConstructor
/**
 * TODO: reword achievement series to only take up a single slot
 *
 * @author Jaiden Baker
 */
public class AchievementSeries {
	@SuppressWarnings("unused")
	private final String name;
	private final List<Achievement> achievements;

	public void register(Plugin plugin) {
		for (Achievement achievement : achievements)
			achievement.register(plugin);
		achievements.get(achievements.size() - 1).setNewLineAfter(true);
	}

}
