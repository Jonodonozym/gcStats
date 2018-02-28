
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
import jdz.statsTracker.GCStatsTrackerConfig;
import jdz.statsTracker.achievement.AchievementDatabase;
import jdz.statsTracker.achievement.AchievementInventories;

@CommandLabel("DEFAULT")
@CommandShortDescription("shows your or another player's cross-server achievements")
@CommandUsage("[server] [player]")
class CommandAchievementDefault extends SubCommand {

	@Override
	public void execute(CommandSender sender, Set<String> flags, String... args) {
		Player player = (Player) sender;

		if (!AchievementDatabase.getInstance().isConnected()) {
			player.sendMessage(ChatColor.RED + "Couldn't connect to the stats and achievements database D:");
			return;
		}

		if (args.length == 0)
			AchievementInventories.openServerAchievements(player, player);

		else if (args.length == 1) {
			if (GCStatsTrackerConfig.servers.contains(args[0]))
				AchievementInventories.openServerAchievements(player, player, args[0]);
			else {
				@SuppressWarnings("deprecation") OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
				if (target.hasPlayedBefore() || target.isOnline())
					AchievementInventories.openServerAchievements(player, target);
				else
					sender.sendMessage(ChatColor.RED + "'" + args[0] + "' is not a valid server or player");
			}
		}
		
		else if (args.length == 2) {
			if (!GCStatsTrackerConfig.servers.contains(args[0])) {
				player.sendMessage(ChatColor.RED+"'"+args[0]+"' is not a valid server");
				return;
			}

			@SuppressWarnings("deprecation") OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
			if (target.hasPlayedBefore() || target.isOnline())
				AchievementInventories.openServerAchievements(player, target, args[0]);
			else
				sender.sendMessage(ChatColor.RED + "'" + args[0] + "' is not a valid player");
			
		}
	}

}
