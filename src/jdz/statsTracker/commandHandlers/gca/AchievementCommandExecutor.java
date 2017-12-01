
package jdz.statsTracker.commandHandlers.gca;

import java.util.Arrays;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import jdz.bukkitUtils.commands.AboutPluginCommand;
import jdz.bukkitUtils.commands.CommandExecutor;
import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandExecutorPlayerOnly;

@CommandExecutorPlayerOnly
public class AchievementCommandExecutor extends CommandExecutor{
	private final List<SubCommand> subcommands;
	
	public AchievementCommandExecutor(JavaPlugin plugin) {
		super(plugin, "gca", false);
		
		subcommands = Arrays.asList(new AchievementShopCommand(),
				new AchievementBalanceCommand(),
				new AboutPluginCommand(plugin));
		
		setDefaultCommand(new AchievementDefaultCommand());
		enableHelpCommand();
	}

	@Override
	protected List<SubCommand> getSubCommands() {
		return subcommands;
	}
}
