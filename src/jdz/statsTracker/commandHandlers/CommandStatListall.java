
package jdz.statsTracker.commandHandlers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandMethod;
import jdz.bukkitUtils.commands.annotations.CommandOpOnly;
import jdz.bukkitUtils.utils.StringUtils;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsManager;

@CommandLabel("list")
@CommandOpOnly
public class CommandStatListall extends SubCommand {

	@CommandMethod
	public void execute(CommandSender sender) {
		List<String> strings = new ArrayList<>();
		for (StatType type : StatsManager.getInstance().enabledStats())
			strings.add(type.getNameNoSpaces());
		if (strings.isEmpty())
			sender.sendMessage("There are no stat types!");
		else
			sender.sendMessage(StringUtils.collectionToString(strings, ", "));
	}

}
