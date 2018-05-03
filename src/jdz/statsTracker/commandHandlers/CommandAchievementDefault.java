
package jdz.statsTracker.commandHandlers;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandShortDescription;
import jdz.bukkitUtils.commands.annotations.CommandUsage;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.achievement.AchievementInventories;
import jdz.statsTracker.achievement.database.AchievementDatabase;

@CommandLabel("DEFAULT")
@CommandShortDescription("shows your or another player's cross-server achievements")
@CommandUsage("[server] [player]")
class CommandAchievementDefault extends SubCommand {

	@Override
	public void execute(CommandSender sender, Set<String> flags, String... args) {
		Player player = (Player) sender;

		if (args.length == 0)
			AchievementInventories.getInstance().openServerAchievements(player, player);

		else if (args.length == 1) {
			if (GCStatsConfig.servers.contains(args[0]))
				AchievementInventories.getInstance().openServerAchievements(player, player, args[0]);
			else {
				Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
					@SuppressWarnings("deprecation") OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
					if (AchievementDatabase.getInstance().hasPlayer(target))
						AchievementInventories.getInstance().openServerAchievements(player, target);
					else
						sender.sendMessage(ChatColor.RED + "'" + args[0] + "' is not a valid server or player");
				});
			}
		}

		else if (args.length == 2) {
			if (!GCStatsConfig.servers.contains(args[0])) {
				player.sendMessage(ChatColor.RED + "'" + args[0] + "' is not a valid server");
				return;
			}


			Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
				@SuppressWarnings("deprecation") OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
				if (AchievementDatabase.getInstance().hasPlayer(target))
					AchievementInventories.getInstance().openServerAchievements(player, target, args[0]);
				else
					sender.sendMessage(ChatColor.RED + "'" + args[0] + "' is not a valid player");
			});

		}
	}

}
