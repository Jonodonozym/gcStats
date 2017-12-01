
package jdz.statsTracker.commandHandlers.gcs;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import jdz.bukkitUtils.commands.CommandExecutor;
import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandExecutorPlayerOnly;

@CommandExecutorPlayerOnly
public class StatsCommandExecutor extends CommandExecutor {
	private final List<SubCommand> subCommands;
	
	public StatsCommandExecutor(JavaPlugin plugin) {
		super(plugin, "gcs", false);
		
		getHelpCommand().addExtraMessage(ChatColor.WHITE+"/gcs [server] - shows your stats for a specific server");
		getHelpCommand().addExtraMessage(ChatColor.WHITE+"/gcs [player] - shows stats for another player");
		getHelpCommand().addExtraMessage(ChatColor.WHITE+"/gcs [player] [server] - shows stats for another player on another server");
		getHelpCommand().addExtraMessage(ChatColor.WHITE+"/gca help - commands for achievements");
		
		setDefaultCommand(new StatsDefaultCommand());
		
		subCommands = Arrays.asList(new ListServersCommand());
	}
	
	@Override
	protected List<SubCommand> getSubCommands() {
		return subCommands;
	}
	
}
