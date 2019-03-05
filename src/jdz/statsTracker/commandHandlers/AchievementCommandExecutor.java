
package jdz.statsTracker.commandHandlers;

import java.util.Arrays;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import jdz.bukkitUtils.commands.AboutPluginCommand;
import jdz.bukkitUtils.commands.CommandExecutor;
import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandExecutorPlayerOnly;
import jdz.statsTracker.GCStats;

@CommandExecutorPlayerOnly
public class AchievementCommandExecutor extends CommandExecutor {
	private final List<SubCommand> subCommands = Arrays.asList(new AboutPluginCommand(GCStats.getInstance()),
			new CommandAchievementPoints(), new CommandAchievementShop());

	public AchievementCommandExecutor(JavaPlugin plugin) {
		super(plugin, "ach", false);
		setDefaultCommand(new CommandAchievementDefault());
	}

	@Override
	protected List<SubCommand> getSubCommands() {
		return subCommands;
	}
}
