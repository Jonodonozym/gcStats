
package jdz.statsTracker.commandHandlers;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandOpOnly;
import jdz.bukkitUtils.commands.annotations.CommandRequiredArgs;
import jdz.bukkitUtils.commands.annotations.CommandUsage;
import jdz.statsTracker.stats.StatsManager;
import jdz.statsTracker.stats.abstractTypes.BufferedStatType;
import jdz.statsTracker.stats.database.StatsDatabase;
import net.md_5.bungee.api.ChatColor;

@CommandLabel("set")
@CommandLabel("s")
@CommandRequiredArgs(3)
@CommandUsage("{statType} {player} {amount}")
@CommandOpOnly
public class CommandStatSet extends SubCommand {

	@Override
	@SuppressWarnings("deprecation")
	public void execute(CommandSender sender, Set<String> flags, String... args) {
		BufferedStatType type = StatsManager.getInstance().getBufferedType(args[0]);
		if (type == null) {
			sender.sendMessage(ChatColor.RED + args[0] + " is not a valid stat type!");
			return;
		}
		
		int amount;
		try {
			amount = Integer.parseInt(args[2]);
		}
		catch (NumberFormatException e) {
			sender.sendMessage(ChatColor.RED + args[2] + " is not a number");
			return;
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
		if (!StatsDatabase.getInstance().hasPlayer(target))
			sender.sendMessage(ChatColor.RED + "That player isn't in the database!");
		else {
			sender.sendMessage(ChatColor.DARK_AQUA + type.getName() + ": " + ChatColor.AQUA + type.get(target));
			sender.sendMessage(ChatColor.DARK_AQUA + "Set to: " + ChatColor.AQUA + amount);
			type.set(target, amount);
		}
	}
}
