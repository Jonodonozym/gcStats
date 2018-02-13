
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
import jdz.statsTracker.GCStatsTracker;
import jdz.statsTracker.stats.StatsDatabase;

public class StatsCommandExecutor extends CommandExecutor {
	private final List<SubCommand> subCommands = Arrays.asList(new AboutPluginCommand(GCStatsTracker.instance),
			CommandStatTop.getInstance(), new CommandStatRank(), new CommandListServers());

	public StatsCommandExecutor(JavaPlugin plugin) {
		super(plugin, "gcs", false);
		setDefaultCommand(new CommandStatDefault());
	}

	@Override
	protected List<SubCommand> getSubCommands() {
		return subCommands;
	}
	
	@Override
	public final void execute(SubCommand command, CommandSender sender, Set<String> flags, String... args) {
		if (!(command instanceof HelpCommand || command instanceof AboutPluginCommand) && !StatsDatabase.getInstance().isConnected())
			sender.sendMessage(ChatColor.RED + "Couldn't connect to the stats and achievements database D:");
		else
			super.execute(command, sender, flags, args);
	}
}
