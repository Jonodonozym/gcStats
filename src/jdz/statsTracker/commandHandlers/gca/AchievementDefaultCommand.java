
package jdz.statsTracker.commandHandlers.gca;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import jdz.statsTracker.achievement.AchievementInventories;
import jdz.statsTracker.commandHandlers.DatabaseCommand;
import jdz.statsTracker.config.Config;
import jdz.statsTracker.stats.PlayTimeRecorder;

class AchievementDefaultCommand extends DatabaseCommand {

	@Override
	protected void execute(Player player, Set<String> flags, String... args) {
		if (args.length == 0) {
			PlayTimeRecorder.getInstance().updateTime(player);
			AchievementInventories.openServerSelect(player, player);
		}

		else {
			@SuppressWarnings("deprecation")
			OfflinePlayer otherPlayer = Bukkit.getOfflinePlayer(args[0]);
			if (db.hasPlayer(Config.serverName, otherPlayer)) {
				if (otherPlayer.isOnline())
					PlayTimeRecorder.getInstance().updateTime((Player) otherPlayer);
				AchievementInventories.openServerSelect(player, otherPlayer);
			} else
				player.sendMessage(ChatColor.RED + "'" + args[0] + "' is not a valid player");
		}
	}

}
