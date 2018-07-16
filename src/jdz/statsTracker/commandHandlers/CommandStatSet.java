
package jdz.statsTracker.commandHandlers;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandMethod;
import jdz.bukkitUtils.commands.annotations.CommandOpOnly;
import jdz.bukkitUtils.commands.annotations.CommandRequiredArgs;
import jdz.bukkitUtils.commands.annotations.CommandUsage;
import jdz.statsTracker.stats.StatsDatabase;
import jdz.statsTracker.stats.abstractTypes.BufferedStatType;
import net.md_5.bungee.api.ChatColor;

@CommandLabel("set")
@CommandRequiredArgs(3)
@CommandUsage("{statType} {player} {amount}")
@CommandOpOnly
public class CommandStatSet extends SubCommand {

	@CommandMethod
	public void set(CommandSender sender, BufferedStatType type, OfflinePlayer player, int amount) {
		if (!StatsDatabase.getInstance().hasPlayer(player))
			sender.sendMessage(ChatColor.RED + "That player isn't in the database!");
		else {
			sender.sendMessage(ChatColor.DARK_AQUA + type.getName() + ": " + ChatColor.AQUA + type.get(player));
			sender.sendMessage(ChatColor.DARK_AQUA + "Set to: " + ChatColor.AQUA + amount);
			type.set(player, amount);
		}
	}
}
