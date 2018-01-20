
package jdz.statsTracker.commandHandlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import jdz.statsTracker.GCStatsTracker;
import jdz.statsTracker.GCStatsTrackerConfig;
import jdz.statsTracker.achievement.AchievementDatabase;
import jdz.statsTracker.achievement.AchievementInventories;
import jdz.statsTracker.achievement.AchievementManager;
import jdz.statsTracker.achievement.AchievementShop;

public class AchievementCommands implements CommandExecutor {
	static String[] gcaHelpMessages = new String[] {
			ChatColor.GRAY + "=============[ " + ChatColor.GOLD + "GCA Help" + ChatColor.GRAY + " ]=============",
			ChatColor.GREEN + "/gcs help "+ChatColor.WHITE+"- commands for stats",
			ChatColor.GREEN + "/gca "+ChatColor.WHITE+"- shows your cross-server achievements",
			ChatColor.GREEN + "/gca [player] "+ChatColor.WHITE+"- shows another player's cross-server achievements",
			ChatColor.GREEN + "/gca points [server] "+ChatColor.WHITE+"- shows the amount of points you have racked up",
			ChatColor.GREEN + "/gca redeem "+ChatColor.WHITE+"- redeem your achievement points for shiny new items!",
			ChatColor.GREEN + "/gca about "+ChatColor.WHITE+"- info about the plugin",
			ChatColor.GRAY + "===================================="

	};

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (cmd.getName().equals(GCStatsTrackerConfig.achCommand)) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("You must be a player to run this command");
				return true;
			}

			Player player = (Player) sender;

			if (args.length == 0) {
				if (AchievementDatabase.getInstance().isConnected())
					AchievementInventories.openServerSelect(player, player);
				else
					player.sendMessage(ChatColor.RED + "Couldn't connect to the stats and achievements database D:");
			}

			else
				switch (args[0].toLowerCase()) {
				case "about":
					sender.sendMessage(StatsCommands.aboutMessages);
					break;
				case "help":
				case "?":
					sender.sendMessage(gcaHelpMessages);
					break;
				case "redeem":
				case "shop":
					if (AchievementDatabase.getInstance().isConnected())
						AchievementShop.openShop(player);
					else
						player.sendMessage(
								ChatColor.RED + "Couldn't connect to the stats and achievements database D:");
					break;
				case "bal":
				case "balance":
				case "points":
					if (AchievementDatabase.getInstance().isConnected()) {
						new BukkitRunnable() {
							@Override
							public void run() {
								if (args.length == 1)
									sender.sendMessage(ChatColor.GREEN + "Achievement Points: " + ChatColor.YELLOW
											+ AchievementManager.getInstance().getAchievementPoints(player));
								else if (GCStatsTrackerConfig.servers.contains(args[1].replaceAll("_", " ")))
									sender.sendMessage(ChatColor.GREEN + "Achievement Points: " + ChatColor.YELLOW
											+ AchievementDatabase.getInstance().getAchievementPoints(player, args[1]));
								else
									sender.sendMessage(ChatColor.RED + "'" + args[1].replaceAll("_", " ")
											+ "' is not a valid server!");
							}
						}.runTaskAsynchronously(GCStatsTracker.instance);
					} else
						player.sendMessage(
								ChatColor.RED + "Couldn't connect to the stats and achievements database D:");
					break;
				default:
					if (AchievementDatabase.getInstance().isConnected()) {
						new BukkitRunnable() {
							@Override
							public void run() {
								@SuppressWarnings("deprecation")
								OfflinePlayer otherPlayer = Bukkit.getOfflinePlayer(args[0]);
								if (otherPlayer.hasPlayedBefore())
									AchievementInventories.openServerSelect(player, otherPlayer);
								else
									sender.sendMessage(ChatColor.RED + "'" + args[0] + "' is not a valid player");
							}
						}.runTaskAsynchronously(GCStatsTracker.instance);
					} else
						player.sendMessage(
								ChatColor.RED + "Couldn't connect to the stats and achievements database D:");
					break;
				}

			return true;
		}
		return false;
	}
}
