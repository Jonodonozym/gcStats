
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
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsManager;
import jdz.statsTracker.stats.database.StatsDatabase;
import net.md_5.bungee.api.ChatColor;

@CommandLabel("inspect")
@CommandLabel("?")
@CommandRequiredArgs(2)
@CommandUsage("{statType} {player}")
@CommandOpOnly
public class CommandStatInspect extends SubCommand {

	@Override
	@SuppressWarnings("deprecation")
	public void execute(CommandSender sender, Set<String> flags, String... args) {
		StatType type = StatsManager.getInstance().getType(args[0]);
		if (type == null) {
			sender.sendMessage(ChatColor.RED + args[0] + " is not a valid stat type!");
			return;
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
		if (!StatsDatabase.getInstance().hasPlayer(target))
			sender.sendMessage(ChatColor.RED + "That player isn't in the database!");
		else
			sender.sendMessage(ChatColor.DARK_AQUA + type.getName() + ": " + ChatColor.AQUA + type.get(target));
	}
}
