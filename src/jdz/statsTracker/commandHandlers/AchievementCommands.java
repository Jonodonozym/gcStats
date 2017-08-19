
package jdz.statsTracker.commandHandlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import jdz.statsTracker.main.Config;

public class AchievementCommands  implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals(Config.achCommand)){
			
		}
		return false;
	}
}
