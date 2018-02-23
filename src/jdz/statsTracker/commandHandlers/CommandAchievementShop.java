
package jdz.statsTracker.commandHandlers;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandShortDescription;
import jdz.statsTracker.achievement.AchievementDatabase;
import jdz.statsTracker.achievement.AchievementShop;


@CommandLabel("shop")
@CommandLabel("redeem")
@CommandShortDescription("redeem your achievement points for shiny new items and rewards!")
class CommandAchievementShop extends SubCommand {

	@Override
	public void execute(CommandSender sender, Set<String> flags, String... args) {
		if (AchievementDatabase.getInstance().isConnected())
			AchievementShop.openShop((Player) sender);
		else
			sender.sendMessage(ChatColor.RED + "Couldn't connect to the stats and achievements database D:");
	}

}
