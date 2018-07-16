
package jdz.statsTracker.objective;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.commands.Command;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandPlayerOnly;
import lombok.Getter;

@CommandLabel("obj")
@CommandPlayerOnly
public class ObjectivesCommand extends Command {
	@Getter private static final ObjectivesCommand instance = new ObjectivesCommand();

	private ObjectivesCommand() {}

	@Override
	public void execute(CommandSender sender, String... args) {
		Player player = (Player) sender;
		ObjectiveManager.displayObjectives(player);
	}

}
