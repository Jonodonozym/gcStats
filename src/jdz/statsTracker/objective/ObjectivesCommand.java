
package jdz.statsTracker.objective;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.commands.Command;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandPlayerOnly;
import lombok.Getter;

@CommandLabel("gco")
@CommandPlayerOnly
public class ObjectivesCommand extends Command {
	@Getter private static final ObjectivesCommand instance = new ObjectivesCommand();

	private ObjectivesCommand() {}

	@Override
	public void execute(CommandSender sender, Set<String> flags, String... args) {
		Player player = (Player) sender;
		ObjectiveManager.displayObjectives(player);
	}

}
