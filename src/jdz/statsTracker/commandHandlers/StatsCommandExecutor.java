
package jdz.statsTracker.commandHandlers;

import java.util.Arrays;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import jdz.bukkitUtils.commands.AboutPluginCommand;
import jdz.bukkitUtils.commands.CommandExecutor;
import jdz.bukkitUtils.commands.SubCommand;
import jdz.statsTracker.GCStats;

public class StatsCommandExecutor extends CommandExecutor {
	private final List<SubCommand> subCommands = Arrays.asList(new AboutPluginCommand(GCStats.getInstance()),
			CommandStatTop.getInstance(), new CommandListServers(), new CommandStatListall(), new CommandStatTest(),
			new CommandStatInspect(), new CommandStatSet());

	public StatsCommandExecutor(JavaPlugin plugin) {
		super(plugin, "gcs", false);
		setDefaultCommand(new CommandStatDefault());
	}

	@Override
	protected List<SubCommand> getSubCommands() {
		return subCommands;
	}
}
