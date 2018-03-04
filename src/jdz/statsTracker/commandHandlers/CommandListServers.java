
package jdz.statsTracker.commandHandlers;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandShortDescription;
import jdz.statsTracker.GCStatsConfig;

@CommandShortDescription("lists avaliable servers")
@CommandLabel("servers")
class CommandListServers extends SubCommand {

	@Override
	public void execute(CommandSender sender, Set<String> flags, String... args) {
		String list = "";
		for (String server : GCStatsConfig.servers)
			list = list + ", " + server.replaceAll(" ", "_");
		list = ChatColor.WHITE + list.substring(2);
		sender.sendMessage(
				ChatColor.GRAY + "============[ " + ChatColor.GOLD + "Server list" + ChatColor.GRAY + " ]============");
		sender.sendMessage(list);
		sender.sendMessage(ChatColor.GRAY + "====================================");
	}

}
