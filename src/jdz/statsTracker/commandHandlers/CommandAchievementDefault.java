
package jdz.statsTracker.commandHandlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandMethod;
import jdz.bukkitUtils.commands.annotations.CommandShortDescription;
import jdz.bukkitUtils.commands.annotations.CommandUsage;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.achievement.AchievementInventories;
import jdz.statsTracker.achievement.database.AchievementDatabase;

@CommandLabel("DEFAULT")
@CommandShortDescription("shows your or another player's cross-server achievements")
@CommandUsage("[server] [player]")
public class CommandAchievementDefault extends SubCommand {

	@CommandMethod
	public void execute(Player player) {
		AchievementInventories.getInstance().openServerAchievements(player, player);
	}

	@CommandMethod
	public void execute(Player player, String serverOrTarget) {

		if (GCStatsConfig.servers.contains(serverOrTarget))
			AchievementInventories.getInstance().openServerAchievements(player, player, serverOrTarget);
		else {
			Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
				@SuppressWarnings("deprecation") OfflinePlayer target = Bukkit.getOfflinePlayer(serverOrTarget);
				if (AchievementDatabase.getInstance().hasPlayer(target))
					AchievementInventories.getInstance().openServerAchievements(player, target);
				else
					player.sendMessage(ChatColor.RED + "'" + serverOrTarget + "' is not a valid server or player");
			});
		}
	}

	@CommandMethod
	public void execute(Player player, String server, OfflinePlayer target) {

		if (!GCStatsConfig.servers.contains(server)) {
			player.sendMessage(ChatColor.RED + "'" + server + "' is not a valid server");
			return;
		}

		Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
			if (AchievementDatabase.getInstance().hasPlayer(target))
				AchievementInventories.getInstance().openServerAchievements(player, target, server);
			else
				player.sendMessage(ChatColor.RED + "'" + server + "' is not a valid player");
		});
	}
}
