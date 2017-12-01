
package jdz.statsTracker.commandHandlers.gcs;

import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jdz.statsTracker.GCStats;
import jdz.statsTracker.commandHandlers.DatabaseCommand;
import jdz.statsTracker.config.Config;
import jdz.statsTracker.stats.PlayTimeRecorder;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsDatabase;

public class StatsDefaultCommand extends DatabaseCommand {

	@Override
	@SuppressWarnings("deprecation")
	protected void execute(Player player, Set<String> flags, String... args) {
		if (args.length == 0)
			showStats(player, Config.serverName, player);

		else if (args.length == 1)
			if (Config.servers.contains(args[0].replaceAll("_", " ")))
				showStats(player, args[0].replaceAll("_", " "), player);
			else if (StatsDatabase.getInstance().hasPlayer(Config.serverName, Bukkit.getOfflinePlayer(args[0])))
				showStats(player, Config.serverName, Bukkit.getOfflinePlayer(args[0]));
			else
				player.sendMessage(ChatColor.RED + "'" + args[0]
						+ "' is not a valid server or that player has never played on this server.");

		// for player AND server
		else if (Config.servers.contains(args[1].replaceAll("_", " "))) {
			if (StatsDatabase.getInstance().hasPlayer(args[1], Bukkit.getOfflinePlayer(args[0])))
				showStats(player, args[1].replaceAll("_", " "), Bukkit.getOfflinePlayer(args[0]));
			else
				player.sendMessage(ChatColor.RED + args[0] + " has never played on that server before!");
		} else
			player.sendMessage(ChatColor.RED + args[1] + " is not a valid server!");
	}

	private void showStats(CommandSender sender, String server, OfflinePlayer offlinePlayer) {
		Bukkit.getScheduler().runTaskAsynchronously(GCStats.plugin, () -> {
			if (offlinePlayer.isOnline())
				PlayTimeRecorder.getInstance().updateTime((Player) offlinePlayer);

			List<String> types = StatsDatabase.getInstance().getEnabledStats(server);
			String[] messages = new String[types.size() + 2];

			int i = 0;
			messages[i++] = ChatColor.GRAY + "============[ " + ChatColor.GOLD + offlinePlayer.getName() + "'s "
					+ server + " stats" + ChatColor.GRAY + " ]============";
			for (String typeStr : types) {
				try {
					StatType type = StatType.valueOf(typeStr);
					messages[i] = ChatColor.DARK_GREEN + type.toPlainString() + ": " + ChatColor.GREEN + ": " + type
							.valueToString(StatsDatabase.getInstance().getStat(offlinePlayer, type.toString(), server));
				} catch (Exception e) {
					messages[i] = ChatColor.DARK_GREEN + typeStr + ": " + ChatColor.GREEN + ": "
							+ StatsDatabase.getInstance().getStat(offlinePlayer, typeStr, server);
				}
				i++;
			}
			messages[i] = ChatColor.GRAY + "";
			for (int ii = 0; ii < messages[0].length() - 10; ii++)
				messages[i] = messages[i] + "=";
			sender.sendMessage(messages);
		});
	}
}
