
package jdz.statsTracker.commandHandlers.gcs;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.statsTracker.commandHandlers.DatabaseCommand;
import jdz.statsTracker.config.Config;

@CommandLabel("servers")
@CommandLabel("listservers")
public class ListServersCommand extends DatabaseCommand{

	@Override
	protected void execute(Player player, Set<String> flags, String... args) {
		player.sendMessage(
				ChatColor.GRAY + "============[ " + ChatColor.GOLD + "Server list" + ChatColor.GRAY + " ]============");
		String list = ChatColor.WHITE + "";
		for (String server : Config.servers)
			list = list + server.replaceAll(" ", "_") + ", ";
		list.substring(0,list.length() - 3);
		player.sendMessage(list);
		player.sendMessage( ChatColor.GRAY + "====================================");
		
	}
}
