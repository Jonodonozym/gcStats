
package jdz.statsTracker.achievement.achievementTypes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import jdz.statsTracker.achievement.Achievement;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
/**
 * TODO: reword achievement series to only take up a single slot
 *
 * @author Jaiden Baker
 */
public class AchievementSeries {
	@SuppressWarnings("unused") private final String name;
	@Getter private final List<Achievement> achievements;

	public AchievementSeries(String name) {
		this(name, new ArrayList<>());
	}

	public void register(Plugin plugin) {
		for (Achievement achievement : achievements)
			achievement.register(plugin);
		getAchievements().get(getAchievements().size() - 1).setNewLineAfter(true);
	}

	public int getProgress(OfflinePlayer player) {
		int progress = 0;
		for (Achievement achievement : getAchievements()) {
			if (!achievement.isAchieved(player))
				break;
			progress++;
		}
		return progress;
	}

	public int size() {
		return getAchievements().size();
	}
}
