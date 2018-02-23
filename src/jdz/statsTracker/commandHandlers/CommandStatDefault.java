package jdz.statsTracker.commandHandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandPermission;
import jdz.bukkitUtils.commands.annotations.CommandShortDescription;
import jdz.bukkitUtils.commands.annotations.CommandUsage;
import jdz.statsTracker.GCStatsTrackerConfig;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsDatabase;
import jdz.statsTracker.stats.StatsManager;

@CommandLabel("DEFAULT")
@CommandShortDescription("Displays your or another player's stats")
@CommandUsage("[player] [server]")
@CommandPermission("gcs.top")
class CommandStatDefault extends SubCommand {

	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, Set<String> flags, String... args) {
		if (args.length == 0) {
			if (sender instanceof Player)
				showStats(sender, (Player) sender);
			else
				sender.sendMessage(ChatColor.RED + "You must be a player to do that!");
			return;
		}

		// for player OR server
		if (args.length == 1) {
			if (GCStatsTrackerConfig.servers.contains(args[0].replaceAll("_", " "))) {
				if (sender instanceof Player)
					showStats(sender, args[0].replaceAll("_", " "), (OfflinePlayer) sender);
				else
					sender.sendMessage(ChatColor.RED + "You must be a player to do that!");
			}
			else {
				OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
				if (target.hasPlayedBefore() || target.isOnline())
					showStats(sender, GCStatsTrackerConfig.serverName, target);
				else
					sender.sendMessage(ChatColor.RED + "'" + args[0]
							+ "' is not a valid server or that player has never played on this server.");
			}
		}

		// for player AND server
		else if (GCStatsTrackerConfig.servers.contains(args[1].replaceAll("_", " "))) {
			OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
			if (target.hasPlayedBefore() || target.isOnline())
				showStats(sender, args[1].replaceAll("_", " "), target);
			else
				sender.sendMessage(ChatColor.RED + args[0] + " has never played on that server before!");
		}
		else
			sender.sendMessage(ChatColor.RED + args[1] + " is not a valid server!");

	}

	private void showStats(CommandSender sender, OfflinePlayer target) {
		List<String> types = new ArrayList<String>(StatsManager.getInstance().enabledStatsSorted().size());
		List<Double> stats = new ArrayList<Double>(StatsManager.getInstance().enabledStatsSorted().size());
		for (StatType type : StatsManager.getInstance().enabledStatsSorted()) {
			if (!type.isVisible())
				continue;
			types.add(type.getName());
			stats.add(type.get(target));
		}

		showStats(sender, target.getName(), "", types, stats);
	}

	private void showStats(CommandSender sender, String server, OfflinePlayer offlinePlayer) {
		if (offlinePlayer.isOnline() && server.equals(GCStatsTrackerConfig.serverName)) {
			showStats(sender, offlinePlayer.getPlayer());
			return;
		}

		List<String> types = StatsDatabase.getInstance().getVisibleStats(server);
		List<Double> stats = new ArrayList<Double>(types.size());
		for (String type : types)
			stats.add(StatsDatabase.getInstance().getStat(offlinePlayer, type.toString(), server));

		showStats(sender, offlinePlayer.getName(), server, types, stats);
	}

	private void showStats(CommandSender sender, String targetName, String server, List<String> types,
			List<Double> stats) {
		String[] messages = new String[types.size() + 2];

		int i = 0;
		messages[i++] = ChatColor.GRAY + "============[ " + ChatColor.GOLD + targetName + "'s"
				+ (server.equals("") ? "" : " " + server) + " stats" + ChatColor.GRAY + " ]============";
		for (String typeStr : types) {
			try {
				StatType type = StatsManager.getInstance().getType(typeStr.replaceAll("_", " "));
				messages[i] = ChatColor.DARK_GREEN + type.getName() + ": " + ChatColor.GREEN + ": "
						+ type.valueToString(stats.get(i - 1));
			}
			catch (Exception e) {
				messages[i] = ChatColor.DARK_GREEN + typeStr + ": " + ChatColor.GREEN + ": " + stats.get(i - 1);
			}
			i++;
		}
		messages[i] = ChatColor.GRAY + "";
		for (int ii = 0; ii < messages[0].length() - 10; ii++)
			messages[i] = messages[i] + "=";
		sender.sendMessage(messages);
	}
}
