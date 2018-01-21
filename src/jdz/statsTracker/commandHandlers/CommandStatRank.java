
package jdz.statsTracker.commandHandlers;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandPermission;
import jdz.bukkitUtils.commands.annotations.CommandPlayerOnly;
import jdz.bukkitUtils.commands.annotations.CommandRequiredArgs;
import jdz.bukkitUtils.commands.annotations.CommandShortDescription;
import jdz.bukkitUtils.commands.annotations.CommandUsage;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsManager;
import net.md_5.bungee.api.ChatColor;

@CommandLabel("rank")
@CommandRequiredArgs(1)
@CommandShortDescription("Displays your rank for a specific stat")
@CommandPlayerOnly
@CommandUsage("rank <statNoSpaces> [player]")
@CommandPermission("gcs.top")
class CommandStatRank extends SubCommand {

	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, Set<String> flags, String... args) {
		OfflinePlayer target = (OfflinePlayer)sender;
		if (args.length == 2){
			target = Bukkit.getOfflinePlayer(args[1]);
			if (!target.hasPlayedBefore()) {
				sender.sendMessage(ChatColor.RED+args[1]+" has never logged in before!");
				return;
			}
		}
		
		StatType type = StatsManager.getInstance().getType(args[0]);
		if (type == null) {
			sender.sendMessage(ChatColor.RED + "'" + args[0]
					+ "' is not a valid stat name! type /gcs to see a list of stats");
			return;
		}
		
		int rank = CommandStatTop.getInstance().playerToRank.get(type).get(target);
		CommandStatTop.getInstance().showStat(sender, type, rank/CommandStatTop.getInstance().playersPerPage);
	}

}
