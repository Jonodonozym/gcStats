
package jdz.statsTracker.commandHandlers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jdz.statsTracker.GCStatsTrackerConfig;
import jdz.statsTracker.achievement.AchievementDatabase;
import jdz.statsTracker.stats.StatsDatabase;
import jdz.statsTracker.stats.StatsManager;
import jdz.statsTracker.stats.StatType;

public class StatsCommands implements CommandExecutor {
	static String[] aboutMessages = new String[] {
			ChatColor.GRAY + "============[ " + ChatColor.GOLD + "GCSA About" + ChatColor.GRAY + " ]=============",
			ChatColor.WHITE + "GuildCraft Stats and Achievements Plugin",
			ChatColor.WHITE + "Keeps track of personal stats and achievements across multiple servers",
			ChatColor.GREEN + "Commands:  " + ChatColor.YELLOW
					+ "/gcs help - stats help   /gca help - achievements help",
			ChatColor.GREEN + "Author:  " + ChatColor.YELLOW + "Jonodonozym",
			ChatColor.GRAY + "======================================" };
	static String[] gcsHelpMessages = new String[] {
			ChatColor.GRAY + "=============[ " + ChatColor.GOLD + "GCS Help" + ChatColor.GRAY + " ]=============",
			ChatColor.WHITE + "/gca help - commands for achievements",
			ChatColor.WHITE + "/gcs - shows your stats for the current server",
			ChatColor.WHITE + "/gcs [server] - shows your stats for a specific server",
			ChatColor.WHITE + "/gcs servers - lists avaliable servers",
			ChatColor.WHITE + "/gcs [player] - shows stats for another player",
			ChatColor.WHITE + "/gcs [player] [server] - shows stats for another player on another server",
			ChatColor.WHITE + "/gcs about - info about the plugin",
			ChatColor.GRAY + "===================================="

	};

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (cmd.getName().equals(GCStatsTrackerConfig.statsCommand)) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("You must be a player to run this command");
				return true;
			}

			Player player = (Player) sender;

			if (args.length == 0)
				if (AchievementDatabase.getInstance().isConnected())
					showStats(sender, GCStatsTrackerConfig.serverName, player);
				else
					player.sendMessage(ChatColor.RED + "Couldn't connect to the stats and achievements database D:");

			else
				switch (args[0].toLowerCase()) {
				case "servers":
					listServers(sender);
					break;
				case "about":
					sender.sendMessage(aboutMessages);
					break;
				case "help":
				case "?":
					sender.sendMessage(gcsHelpMessages);
					break;
				case "shop":
					break;
				case "bal":
				case "balance":
				case "points":
					break;
				default:
					if (AchievementDatabase.getInstance().isConnected()) {
						// for player OR server
						if (args.length == 1) {
							if (GCStatsTrackerConfig.servers.contains(args[0].replaceAll("_", " "))) {
								showStats(sender, args[0].replaceAll("_", " "), player);
								break;
							}
							OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
							if (target.hasPlayedBefore())
								showStats(sender, GCStatsTrackerConfig.serverName, target);
							else
								sender.sendMessage(ChatColor.RED + "'" + args[0]
										+ "' is not a valid server or that player has never played on this server.");
						}

						// for player AND server
						else if (GCStatsTrackerConfig.servers.contains(args[1].replaceAll("_", " "))) {
							OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
							if (target.hasPlayedBefore())
								showStats(sender, args[1].replaceAll("_", " "), target);
							else
								sender.sendMessage(
										ChatColor.RED + args[0] + " has never played on that server before!");
						} else
							sender.sendMessage(ChatColor.RED + args[1] + " is not a valid server!");
					} else
						player.sendMessage(
								ChatColor.RED + "Couldn't connect to the stats and achievements database D:");
				}

			return true;
		}
		return false;
	}

	static void listServers(CommandSender sender) {
		sender.sendMessage(
				ChatColor.GRAY + "============[ " + ChatColor.GOLD + "Server list" + ChatColor.GRAY + " ]============");
		String list = ChatColor.WHITE + "";
		for (String server : GCStatsTrackerConfig.servers)
			list = list + server.replaceAll(" ", "_") + ", ";
		list.substring(0, list.length() - 3);
		sender.sendMessage(list);
		sender.sendMessage(ChatColor.GRAY + "====================================");
	}

	private void showStats(CommandSender sender, String server, OfflinePlayer offlinePlayer) {
		if (offlinePlayer.isOnline() && server.equals(GCStatsTrackerConfig.serverName)) {
			showStats(sender, offlinePlayer.getPlayer());
			return;
		}

		List<String> types = StatsDatabase.getInstance().getEnabledStats(server);
		List<Double> stats = new ArrayList<Double>(types.size());
		for (String type: types)
			stats.add(StatsDatabase.getInstance().getStat(offlinePlayer, type.toString(), server));
			
		showStats(sender, offlinePlayer.getName(), server, types, stats);
	}

	private void showStats(CommandSender sender, Player target) {

		List<String> types = new ArrayList<String>(StatsManager.getInstance().enabledStatsSorted().size());
		List<Double> stats = new ArrayList<Double>(StatsManager.getInstance().enabledStatsSorted().size());
		for (StatType type: StatsManager.getInstance().enabledStatsSorted()){
			types.add(type.getName());
			stats.add(type.get(target));
		}
			
		showStats(sender, target.getName(), "", types, stats);
	}
	
	private void showStats(CommandSender sender, String targetName, String server, List<String> types, List<Double> stats) {
		String[] messages = new String[types.size() + 2];

		int i = 0;
		messages[i++] = ChatColor.GRAY + "============[ " + ChatColor.GOLD + targetName + "'s" + (server.equals("")?"":" "+server)
				+ " stats" + ChatColor.GRAY + " ]============";
		for (String typeStr : types) {
			try {
				StatType type = StatsManager.getInstance().getType(typeStr.replaceAll("_", " "));
				messages[i] = ChatColor.DARK_GREEN + type.getName() + ": " + ChatColor.GREEN + ": "
						+ type.valueToString(stats.get(i-1));
			} catch (Exception e) {
				messages[i] = ChatColor.DARK_GREEN + typeStr + ": " + ChatColor.GREEN + ": "
						+ stats.get(i-1);
			}
			i++;
		}
		messages[i] = ChatColor.GRAY + "";
		for (int ii = 0; ii < messages[0].length() - 10; ii++)
			messages[i] = messages[i] + "=";
		sender.sendMessage(messages);
	}
}
