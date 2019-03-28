
package jdz.statsTracker.commandHandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import static org.bukkit.ChatColor.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandMethod;
import jdz.bukkitUtils.commands.annotations.CommandRequiredArgs;
import jdz.bukkitUtils.commands.annotations.CommandShortDescription;
import jdz.bukkitUtils.commands.annotations.CommandUsage;
import jdz.bukkitUtils.utils.StringUtils;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsDatabase;
import lombok.Getter;

@CommandLabel("top")
@CommandLabel("rank")
@CommandRequiredArgs(1)
@CommandShortDescription("Displays the top scores for a specific stat")
@CommandUsage("top <statNoSpaces> [pageNumber]")
public class CommandStatTop extends SubCommand {
	@Getter private static final CommandStatTop instance = new CommandStatTop();

	private CommandStatTop() {}

	private final Map<StatType, List<String>> playersSorted = new HashMap<StatType, List<String>>();
	@Getter private final Map<StatType, Map<String, Integer>> playerToRank = new HashMap<StatType, Map<String, Integer>>();
	private final Map<StatType, Map<String, Double>> playerToStat = new HashMap<StatType, Map<String, Double>>();

	private final Map<StatType, Long> lastUpdates = new HashMap<StatType, Long>();
	private final long timeBetweenUpdates = 120000;
	final int playersPerPage = 8;

	@CommandMethod
	public void findTop(CommandSender sender, StatType type) {
		findTop(sender, type, 1);
	}

	@CommandMethod
	public void findTop(CommandSender sender, StatType type, int pageNumber) {
		if (!lastUpdates.containsKey(type) || lastUpdates.get(type) < System.currentTimeMillis() - timeBetweenUpdates)
			updateStat(sender, type);
		showStat(sender, type, pageNumber - 1);
	}

	private void updateStat(CommandSender sender, StatType type) {
		Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
			int entries = StatsDatabase.getInstance().countEntries(GCStatsConfig.serverName);
			if (entries > 1000)
				sender.sendMessage(GOLD + "Sorting " + AQUA + entries + GOLD + " entries, please wait...");
		});

		Map<String, Double> rows = StatsDatabase.getInstance().getAllSorted(type);

		List<String> players = new ArrayList<String>();
		Map<String, Integer> ranks = new HashMap<String, Integer>();
		Map<String, Double> stats = new HashMap<String, Double>();

		int i = 0;
		for (Map.Entry<String, Double> row : rows.entrySet()) {
			String player = row.getKey();
			players.add(player);
			ranks.put(player, i++);
			stats.put(player, row.getValue());
		}

		playersSorted.put(type, players);
		playerToRank.put(type, ranks);
		playerToStat.put(type, stats);

		lastUpdates.put(type, System.currentTimeMillis());
	}

	void showStat(CommandSender sender, StatType type, int pageIndex) {
		if (pageIndex < 0)
			pageIndex = 0;

		int maxPage = Math.max(0, playersSorted.get(type).size() / playersPerPage);
		if (pageIndex > maxPage)
			pageIndex = maxPage;

		int min = pageIndex * playersPerPage;
		int max = Math.min(min + playersPerPage - 1, playersSorted.get(type).size() - 1);

		String[] messages = new String[max - min + 3];

		int offset = 1;
		if (sender instanceof Player) {
			messages = new String[max - min + 5];
			offset = 3;
			Player player = (Player) sender;
			int rank = playerToRank.get(type).get(player.getName());
			String value = type.valueToString(playerToStat.get(type).get(player.getName()));
			String name = player.getName();
			messages[1] = AQUA + "[" + (rank + 1) + "] " + GREEN + name + WHITE + "  " + value;
		}

		messages[0] = GRAY + " ============[ " + DARK_AQUA + "Top " + type.getName()
				+ (pageIndex == 0 ? "" : ", " + GREEN + "Page " + (pageIndex + 1) + " / " + (maxPage + 1)) + GRAY
				+ " ]============";

		messages[messages.length - 1] = GRAY + StringUtils.repeat("=", messages[0].length() - 9);

		for (int i = min; i <= max; i++) {
			String player = playersSorted.get(type).get(i);
			String value = type.valueToString(playerToStat.get(type).get(player));
			messages[i - min + offset] = GOLD + "[" + (i + 1) + "] " + GREEN + player + WHITE + "  " + value;
		}

		sender.sendMessage(messages);
	}
}
