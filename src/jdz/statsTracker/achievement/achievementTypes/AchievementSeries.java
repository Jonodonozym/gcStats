
package jdz.statsTracker.achievement.achievementTypes;

import java.util.List;

import org.bukkit.OfflinePlayer;
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
	@SuppressWarnings("unused") private final String name;
	private final List<Achievement> achievements;

	public void register(Plugin plugin) {
		for (Achievement achievement : achievements)
			achievement.register(plugin);
		achievements.get(achievements.size() - 1).setNewLineAfter(true);
	}

	public int getProgress(OfflinePlayer player) {
		int progress = 0;
		for (Achievement achievement : achievements) {
			if (!achievement.isAchieved(player))
				break;
			progress++;
		}
		return progress;
	}

	public int size() {
		return achievements.size();
	}
}
