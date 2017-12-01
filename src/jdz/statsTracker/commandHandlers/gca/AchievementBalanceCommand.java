
package jdz.statsTracker.commandHandlers.gca;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandPermission;
import jdz.bukkitUtils.commands.annotations.CommandShortDescription;
import jdz.bukkitUtils.commands.annotations.CommandUsage;
import jdz.statsTracker.commandHandlers.DatabaseCommand;
import jdz.statsTracker.config.Config;

@CommandLabel("bal")
@CommandLabel("balanace")
@CommandLabel("amount")
@CommandUsage("/gca bal <server>")
@CommandShortDescription("Achievement point balance for the current or specified server")
@CommandPermission("gca.shop")
class AchievementBalanceCommand extends DatabaseCommand {

	@Override
	public void execute(Player player, Set<String> flags, String... args) {
		if (args.length == 1)
			player.sendMessage(
					ChatColor.GREEN + "Achievement Points: " + ChatColor.YELLOW + db.getAchievementPoints(player));
		else if (Config.servers.contains(args[1].replaceAll("_", " ")))
			player.sendMessage(ChatColor.GREEN + "Achievement Points: " + ChatColor.YELLOW
					+ db.getAchievementPoints(player, args[1]));
		else
			player.sendMessage(ChatColor.RED + "'" + args[1].replaceAll("_", " ") + "' is not a valid server!");
	}

}
