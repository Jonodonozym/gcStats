
package jdz.statsTracker.commandHandlers;

import java.util.Arrays;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import jdz.bukkitUtils.commands.AboutPluginCommand;
import jdz.bukkitUtils.commands.CommandArgumentParsers;
import jdz.bukkitUtils.commands.CommandExecutor;
import jdz.bukkitUtils.commands.SubCommand;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsManager;
import jdz.statsTracker.stats.abstractTypes.BufferedStatType;

public class StatsCommandExecutor extends CommandExecutor {
	static {
		CommandArgumentParsers.addParser(StatType.class, (s) -> {
			StatType type = StatsManager.getInstance().getType(s);
			if (type == null)
				throw new IllegalArgumentException(s + " is not a valid stat type!");
			return type;
		});
		CommandArgumentParsers.addParser(BufferedStatType.class, (s) -> {
			BufferedStatType type = StatsManager.getInstance().getBufferedType(s);
			if (type == null)
				throw new IllegalArgumentException(s + " is not a valid stat type!");
			return type;
		});
	}

	private final List<SubCommand> subCommands = Arrays.asList(new AboutPluginCommand(GCStats.getInstance()),
			CommandStatTop.getInstance(), new CommandListServers(), new CommandStatListall(), new CommandStatInspect(),
			new CommandStatSet());

	public StatsCommandExecutor(JavaPlugin plugin) {
		super(plugin, "s", false);
		setDefaultCommand(new CommandStatDefault());
	}

	@Override
	protected List<SubCommand> getSubCommands() {
		return subCommands;
	}
}
