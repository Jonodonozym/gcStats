
package jdz.statsTracker.commandHandlers;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandShortDescription;
import jdz.bukkitUtils.commands.annotations.CommandUsage;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.achievement.AchievementManager;
import jdz.statsTracker.database.AchievementDatabase;

@CommandLabel("bal")
@CommandLabel("balance")
@CommandLabel("points")
@CommandShortDescription("shows the amount of points you have racked up")
@CommandUsage("bal [server]")
class CommandAchievementPoints extends SubCommand {

	@Override
	public void execute(CommandSender sender, Set<String> flags, String... args) {
		Player player = (Player) sender;

		Bukkit.getScheduler().runTaskAsynchronously(GCStats.instance, () -> {
			if (args.length == 0)
				sender.sendMessage(ChatColor.GREEN + "Achievement Points: " + ChatColor.YELLOW
						+ AchievementManager.getInstance().getAchievementPoints(player));
			else if (GCStatsConfig.servers.contains(args[0].replaceAll("_", " ")))
				sender.sendMessage(ChatColor.GREEN + "Achievement Points: " + ChatColor.YELLOW
						+ AchievementDatabase.getInstance().getAchievementPoints(player, args[1]));
			else
				sender.sendMessage(ChatColor.RED + "'" + args[1].replaceAll("_", " ") + "' is not a valid server!");
		});
	}

}
