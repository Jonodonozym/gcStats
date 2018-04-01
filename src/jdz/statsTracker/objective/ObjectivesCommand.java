
package jdz.statsTracker.objective;

import java.util.HashSet;
import java.util.Set;

import static org.bukkit.ChatColor.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.commands.Command;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandPlayerOnly;
import jdz.statsTracker.GCStats;
import lombok.Getter;
import lombok.Setter;

@CommandLabel("gco")
@CommandPlayerOnly
public class ObjectivesCommand extends Command {
	@Getter private static final ObjectivesCommand instance = new ObjectivesCommand();

	private ObjectivesCommand() {
		register(GCStats.getInstance());
	}

	@Setter private static boolean unlockedObjectivesShown = true;

	@Override
	public void execute(CommandSender sender, Set<String> flags, String... args) {
		Player player = (Player) sender;

		Set<Objective> objectives = Objective.getObjectives(player);
		Set<Objective> lockedObjectives = new HashSet<Objective>(objectives);
		for (Objective o : Objective.getObjectives(player))
			if (o.isUnlocked(player))
				lockedObjectives.remove(o);

		if (!unlockedObjectivesShown && lockedObjectives.isEmpty() || objectives.isEmpty()) {
			player.sendMessage(AQUA + "You don't have any more objectives to complete");
			return;
		}

		int completed = objectives.size() - lockedObjectives.size();

		player.sendMessage(GREEN + "You have completed " + completed + " / " + objectives.size() + " objectives: ");
		displayObjectives(player, unlockedObjectivesShown?objectives:lockedObjectives);
	}
	
	public static void displayObjectives(Player player, Set<Objective> objectives) {
		for (Objective objective : objectives) {
			String message = "\t- " + (objective.isUnlocked(player) ? GREEN : RED);
			if (!objective.getName().equals(""))
				message += objective.getName() + ": ";
			message += objective.getDescription();

			if (!objective.getRewardText().equals(""))
				message += WHITE + "" + RESET + " (" + objective.getRewardText() + WHITE + RESET + ")";

			player.sendMessage(message);
		}
	}

}
