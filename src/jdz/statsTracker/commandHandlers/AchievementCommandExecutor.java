
package jdz.statsTracker.commandHandlers;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import jdz.bukkitUtils.commands.AboutPluginCommand;
import jdz.bukkitUtils.commands.CommandExecutor;
import jdz.bukkitUtils.commands.HelpCommand;
import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandExecutorPlayerOnly;
import jdz.statsTracker.GCStatsTracker;
import jdz.statsTracker.achievement.AchievementDatabase;

@CommandExecutorPlayerOnly
public class AchievementCommandExecutor extends CommandExecutor {
	private final List<SubCommand> subCommands = Arrays.asList(new AboutPluginCommand(GCStatsTracker.instance),
			new CommandAchievementPoints(), new CommandAchievementShop());

	public AchievementCommandExecutor(JavaPlugin plugin) {
		super(plugin, "gca", false);
		setDefaultCommand(new CommandAchievementDefault());
	}

	@Override
	protected List<SubCommand> getSubCommands() {
		return subCommands;
	}

	@Override
	public final void execute(SubCommand command, CommandSender sender, Set<String> flags, String... args) {
		if (!(command instanceof HelpCommand || command instanceof AboutPluginCommand)
				&& !AchievementDatabase.getInstance().isConnected())
			sender.sendMessage(ChatColor.RED + "Couldn't connect to the stats and achievements database D:");
		else
			super.execute(command, sender, flags, args);
	}
}
