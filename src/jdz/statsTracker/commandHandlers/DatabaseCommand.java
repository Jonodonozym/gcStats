
package jdz.statsTracker.commandHandlers;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.statsTracker.stats.StatsDatabase;

public abstract class DatabaseCommand extends SubCommand {
	protected static StatsDatabase db;
	
	public DatabaseCommand() {
		if (db == null)
				db = StatsDatabase.getInstance();
	}
	
	@Override
	public void execute(CommandSender sender, Set<String> flags, String... args) {
		if (db.getApi().isConnected())
			execute((Player)sender, flags, args);
		else
			sender.sendMessage(ChatColor.RED+"Couldn't connect to the stats and achievements database D:");
	}
	
	protected abstract void execute(Player player, Set<String> flags, String...args);

}
