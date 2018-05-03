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
import jdz.bukkitUtils.misc.Pair;
import jdz.bukkitUtils.misc.StringUtils;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsManager;
import jdz.statsTracker.stats.database.StatsDatabase;

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
			if (GCStatsConfig.servers.contains(args[0].replaceAll("_", " "))) {
				if (sender instanceof Player)
					showStats(sender, args[0].replaceAll("_", " "), (OfflinePlayer) sender);
				else
					sender.sendMessage(ChatColor.RED + "You must be a player to do that!");
			}
			else {
				Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
					OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
					if (StatsDatabase.getInstance().hasPlayer(target))
						showStats(sender, GCStatsConfig.serverName, target);
					else
						sender.sendMessage(ChatColor.RED + "'" + args[0]
								+ "' is not a valid server or that player has never played on this server.");
				});
			}
			return;
		}
		
		if (GCStatsConfig.servers.contains(args[1].replaceAll("_", " "))) {
			Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
				OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
				if (StatsDatabase.getInstance().hasPlayer(target))
					showStats(sender, args[1].replaceAll("_", " "), target);
				else
					sender.sendMessage(ChatColor.RED + args[0] + " has never played on that server before!");
			});
		}
		else
			sender.sendMessage(ChatColor.RED + args[1] + " is not a valid server!");


	}

	private void showStats(CommandSender sender, OfflinePlayer target) {
		Pair<List<String>, List<Double>> stats = getStats(GCStatsConfig.serverName, target);

		showStats(sender, target.getName(), GCStatsConfig.serverName, stats.getKey(), stats.getValue());
	}

	private Pair<List<String>, List<Double>> getStats(String serverName, OfflinePlayer player) {
		List<String> types = new ArrayList<String>();
		List<Double> stats = new ArrayList<Double>();

		if (serverName.equals(GCStatsConfig.serverName))
			for (StatType type : StatsManager.getInstance().enabledStatsSorted()) {
				if (!type.isVisible())
					continue;
				types.add(type.getNameUnderscores());
				stats.add(type.get(player));
			}
		else {
			types = StatsDatabase.getInstance().getVisibleStats(serverName);
			for (String type : types)
				stats.add(StatsDatabase.getInstance().getStat(player, type, serverName));
		}

		return new Pair<List<String>, List<Double>>(types, stats);
	}

	private void showStats(CommandSender sender, String server, OfflinePlayer offlinePlayer) {
		if (offlinePlayer.isOnline() && server.equals(GCStatsConfig.serverName)) {
			showStats(sender, offlinePlayer.getPlayer());
			return;
		}

		Pair<List<String>, List<Double>> stats = getStats(server, offlinePlayer);

		showStats(sender, offlinePlayer.getName(), server, stats.getKey(), stats.getValue());
	}

	private void showStats(CommandSender sender, String targetName, String server, List<String> types,
			List<Double> stats) {
		String[] messages = new String[types.size() + 2];

		int i = 0;
		messages[i++] = ChatColor.GRAY + "============[ " + ChatColor.GOLD + targetName + "'s"
				+ (server.equals("") || server.equals(GCStatsConfig.serverName) ? "" : " " + server) + " stats"
				+ ChatColor.GRAY + " ]============";
		for (String typeStr : types) {
			try {
				StatType type = StatsManager.getInstance().getType(typeStr.replaceAll("_", " "));
				messages[i] = ChatColor.DARK_AQUA + type.getName() + ChatColor.AQUA + ": "
						+ type.valueToString(stats.get(i - 1));
			}
			catch (Exception e) {
				messages[i] = ChatColor.DARK_AQUA + typeStr + ChatColor.AQUA + ": " + stats.get(i - 1);
			}
			i++;
		}
		messages[i] = ChatColor.GRAY + StringUtils.repeat("=", messages[0].length() - 9);
		sender.sendMessage(messages);
	}
}
