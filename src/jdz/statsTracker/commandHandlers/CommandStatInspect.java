
package jdz.statsTracker.commandHandlers;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandMethod;
import jdz.bukkitUtils.commands.annotations.CommandOpOnly;
import jdz.bukkitUtils.commands.annotations.CommandRequiredArgs;
import jdz.bukkitUtils.commands.annotations.CommandUsage;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsDatabase;

@CommandLabel("inspect")
@CommandLabel("?")
@CommandRequiredArgs(2)
@CommandUsage("inspect {statType} {player}")
@CommandOpOnly
public class CommandStatInspect extends SubCommand {

	@CommandMethod
	public void execute(CommandSender sender, StatType type, OfflinePlayer target) {
		if (!StatsDatabase.getInstance().hasPlayer(target))
			sender.sendMessage(ChatColor.RED + "That player isn't in the database!");
		else
			sender.sendMessage(ChatColor.DARK_AQUA + type.getName() + ": " + ChatColor.AQUA + type.get(target));
	}
}
