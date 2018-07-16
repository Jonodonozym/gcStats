
package jdz.statsTracker.commandHandlers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandMethod;
import jdz.bukkitUtils.commands.annotations.CommandShortDescription;
import jdz.bukkitUtils.commands.annotations.CommandUsage;
import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.achievement.AchievementManager;
import jdz.statsTracker.achievement.database.AchievementDatabase;

@CommandLabel("bal")
@CommandLabel("balance")
@CommandLabel("points")
@CommandShortDescription("shows the amount of points you have racked up")
@CommandUsage("bal [server]")
public class CommandAchievementPoints extends SubCommand {

	@CommandMethod
	public void show(Player sender) {
		sender.sendMessage(ChatColor.GREEN + "Achievement Points: " + ChatColor.YELLOW
				+ AchievementManager.getInstance().getAchievementPoints(sender));
	}

	@CommandMethod
	public void show(Player sender, String server) {
		if (GCStatsConfig.servers.contains(server.replaceAll("_", " ")))
			sender.sendMessage(ChatColor.GREEN + "Achievement Points: " + ChatColor.YELLOW
					+ AchievementDatabase.getInstance().getAchievementPoints(sender, server));
		else
			sender.sendMessage(ChatColor.RED + "'" + server.replaceAll("_", " ") + "' is not a valid server!");
	}

}
