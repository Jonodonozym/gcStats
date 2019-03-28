package jdz.statsTracker.commandHandlers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import static org.bukkit.ChatColor.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandMethod;
import jdz.bukkitUtils.commands.annotations.CommandShortDescription;
import jdz.bukkitUtils.commands.annotations.CommandUsage;
import jdz.bukkitUtils.utils.StringUtils;
import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsDatabase;
import jdz.statsTracker.stats.StatsManager;
import jdz.statsTracker.stats.abstractTypes.NoSaveStatType;

@CommandLabel("DEFAULT")
@CommandShortDescription("Displays your or another player's stats")
@CommandUsage("[player] [server]")
public class CommandStatDefault extends SubCommand {

	@CommandMethod
	public void checkSelf(CommandSender sender) {
		if (sender instanceof Player)
			showStats(sender, (Player) sender);
		else
			sender.sendMessage(RED + "You must be a player to do that!");
	}

	@CommandMethod
	public void checkPlayerOrServer(CommandSender sender, String playerOrServer) {
		if (GCStatsConfig.servers.contains(playerOrServer.replaceAll("_", " "))) {
			if (sender instanceof Player)
				showStats(sender, playerOrServer.replaceAll("_", " "), (OfflinePlayer) sender);
			else
				sender.sendMessage(RED + "You must be a player to do that!");
			return;
		}

		@SuppressWarnings("deprecation")
		OfflinePlayer target = Bukkit.getOfflinePlayer(playerOrServer);
		if (StatsDatabase.getInstance().hasPlayer(target))
			showStats(sender, GCStatsConfig.serverName, target);
		else
			sender.sendMessage(RED + "'" + playerOrServer
					+ "' is not a valid server or that player has never played on this server.");
	}

	@CommandMethod
	public void checkPlayerServer(CommandSender sender, OfflinePlayer player, String server) {
		if (StatsDatabase.getInstance().hasPlayer(player))
			showStats(sender, server, player);
		else
			sender.sendMessage(RED + "'" + player.getName()
					+ "' is not a valid server or that player has never played on this server.");
	}

	private void showStats(CommandSender sender, OfflinePlayer target) {
		Map<String, Double> stats = getStats(GCStatsConfig.serverName, target);
		showStats(sender, target.getName(), GCStatsConfig.serverName, stats);
	}

	private Map<String, Double> getStats(String serverName, OfflinePlayer player) {
		Map<String, Double> stats = new LinkedHashMap<String, Double>();
		for (StatType type : StatsManager.getInstance().enabledStatsSorted())
			if (type.isVisible())
				stats.put(type.getNameUnderscores(), type.getDefault());

		if (serverName.equals(GCStatsConfig.serverName)) {
			if (player.isOnline())
				for (StatType type : StatsManager.getInstance().getVisibleTypes())
					stats.put(type.getNameUnderscores(), type.get(player));
			else {
				List<String> types = new ArrayList<String>();
				for (StatType type : StatsManager.getInstance().getVisibleTypes())
					if (!(type instanceof NoSaveStatType))
						types.add(type.getNameUnderscores());
				Map<String, Double> unsortedStats = StatsDatabase.getInstance().getStats(player, types, serverName);
				for (String type : stats.keySet())
					stats.put(type, unsortedStats.get(type));
			}

			return stats;
		}

		List<String> types = StatsDatabase.getInstance().getVisibleStats(serverName);
		return StatsDatabase.getInstance().getStats(player, types, serverName);
	}

	private void showStats(CommandSender sender, String server, OfflinePlayer offlinePlayer) {
		if (offlinePlayer.isOnline() && server.equals(GCStatsConfig.serverName)) {
			showStats(sender, offlinePlayer.getPlayer());
			return;
		}

		showStats(sender, offlinePlayer.getName(), server, getStats(server, offlinePlayer));
	}

	private void showStats(CommandSender sender, String targetName, String server, Map<String, Double> stats) {
		String[] messages = new String[stats.size() + 2];

		int i = 0;
		messages[i++] = GRAY + "============[ " + GOLD + targetName + "'s"
				+ (server.equals("") || server.equals(GCStatsConfig.serverName) ? "" : " " + server) + " stats" + GRAY
				+ " ]============";
		for (Entry<String, Double> entry : stats.entrySet()) {
			try {
				StatType type = StatsManager.getInstance().getType(entry.getKey());
				messages[i] = DARK_AQUA + type.getName() + AQUA + ": " + type.valueToString(entry.getValue());
			}
			catch (Exception e) {
				messages[i] = DARK_AQUA + entry.getKey() + AQUA + ": " + entry.getValue();
			}
			i++;
		}
		messages[i] = GRAY + StringUtils.repeat("=", messages[0].length() - 9);
		sender.sendMessage(messages);
	}
}
