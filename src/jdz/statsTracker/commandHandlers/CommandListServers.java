
package jdz.statsTracker.commandHandlers;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandShortDescription;
import jdz.statsTracker.GCStatsTrackerConfig;

@CommandLabel("servers")
@CommandShortDescription("lists avaliable servers")
class CommandListServers extends SubCommand{

	@Override
	public void execute(CommandSender sender, Set<String> flags, String... args) {
		sender.sendMessage(
				ChatColor.GRAY + "============[ " + ChatColor.GOLD + "Server list" + ChatColor.GRAY + " ]============");
		String list = ChatColor.WHITE + "";
		for (String server : GCStatsTrackerConfig.servers)
			list = list + server.replaceAll(" ", "_") + ", ";
		list.substring(0, list.length() - 3);
		sender.sendMessage(list);
		sender.sendMessage(ChatColor.GRAY + "====================================");
	}

}
