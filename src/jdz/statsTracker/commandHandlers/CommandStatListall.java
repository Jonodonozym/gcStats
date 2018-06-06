
package jdz.statsTracker.commandHandlers;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandOpOnly;
import jdz.bukkitUtils.misc.StringUtils;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsManager;

@CommandLabel("list")
@CommandOpOnly
public class CommandStatListall extends SubCommand{

	@Override
	public void execute(CommandSender sender, String... args) {
		List<String> strings = new ArrayList<String>();
		for (StatType type: StatsManager.getInstance().enabledStats())
			strings.add(type.getNameNoSpaces());
		if (strings.isEmpty())
			sender.sendMessage("There are no stat types!");
		else
			sender.sendMessage(StringUtils.collectionToString(strings, ", "));
	}

}
