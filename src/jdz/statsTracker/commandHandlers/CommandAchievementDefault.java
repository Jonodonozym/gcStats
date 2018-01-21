
package jdz.statsTracker.commandHandlers;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandShortDescription;
import jdz.bukkitUtils.commands.annotations.CommandUsage;
import jdz.statsTracker.achievement.AchievementDatabase;
import jdz.statsTracker.achievement.AchievementInventories;

@CommandShortDescription("shows your or another player's cross-server achievements")
@CommandUsage("/gca\n/gca [player]")
class CommandAchievementDefault extends SubCommand {

	@Override
	public void execute(CommandSender sender, Set<String> flags, String... args) {
		Player player = (Player) sender;

		if (!AchievementDatabase.getInstance().isConnected()) {
			player.sendMessage(ChatColor.RED + "Couldn't connect to the stats and achievements database D:");
			return;
		}

		if (args.length == 0)
			AchievementInventories.openServerSelect(player, player);

		else {
			@SuppressWarnings("deprecation")
			OfflinePlayer otherPlayer = Bukkit.getOfflinePlayer(args[0]);
			if (otherPlayer.hasPlayedBefore())
				AchievementInventories.openServerSelect(player, otherPlayer);
			else
				sender.sendMessage(ChatColor.RED + "'" + args[0] + "' is not a valid player");
		}
	}

}
