
package jdz.statsTracker.commandHandlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import jdz.statsTracker.main.Config;

public class StatsCommands implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals(Config.statsCommand)){
			
		}
		return false;
	}

}
