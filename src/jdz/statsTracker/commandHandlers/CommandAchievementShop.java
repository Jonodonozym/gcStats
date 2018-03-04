
package jdz.statsTracker.commandHandlers;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandShortDescription;
import jdz.statsTracker.achievement.AchievementShop;


@CommandLabel("shop")
@CommandLabel("redeem")
@CommandShortDescription("redeem your achievement points for shiny new items and rewards!")
class CommandAchievementShop extends SubCommand {

	@Override
	public void execute(CommandSender sender, Set<String> flags, String... args) {
		AchievementShop.openShop((Player) sender);
	}

}
