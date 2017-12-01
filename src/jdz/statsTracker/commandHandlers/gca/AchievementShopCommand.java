
package jdz.statsTracker.commandHandlers.gca;

import java.util.Set;

import org.bukkit.entity.Player;

import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandPermission;
import jdz.bukkitUtils.commands.annotations.CommandShortDescription;
import jdz.bukkitUtils.commands.annotations.CommandUsage;
import jdz.statsTracker.achievement.AchievementShop;
import jdz.statsTracker.commandHandlers.DatabaseCommand;

@CommandLabel("shop")
@CommandLabel("redeem")
@CommandUsage("/gca shop")
@CommandShortDescription("Redeem achievement points for items")
@CommandPermission("gca.shop")
class AchievementShopCommand extends DatabaseCommand{

	@Override
	protected void execute(Player player, Set<String> flags, String... args) {
		AchievementShop.openShop(player);
	}

}
