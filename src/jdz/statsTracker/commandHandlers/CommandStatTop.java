
package jdz.statsTracker.commandHandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandRequiredArgs;
import jdz.bukkitUtils.commands.annotations.CommandShortDescription;
import jdz.bukkitUtils.commands.annotations.CommandUsage;
import jdz.bukkitUtils.misc.StringUtils;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsManager;
import jdz.statsTracker.stats.database.StatsDatabase;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@CommandLabel("top")
@CommandLabel("rank")
@CommandRequiredArgs(1)
@CommandShortDescription("Displays the top scores for a specific stat")
@CommandUsage("top <statNoSpaces> [pageNumber]")
class CommandStatTop extends SubCommand {
	@Getter private static final CommandStatTop instance = new CommandStatTop();

	private CommandStatTop() {}

	private final Map<StatType, List<String>> playersSorted = new HashMap<StatType, List<String>>();
	@Getter private final Map<StatType, Map<String, Integer>> playerToRank = new HashMap<StatType, Map<String, Integer>>();
	private final Map<StatType, Map<String, Double>> playerToStat = new HashMap<StatType, Map<String, Double>>();

	private final Map<StatType, Long> lastUpdates = new HashMap<StatType, Long>();
	private final long timeBetweenUpdates = 120000;
	final int playersPerPage = 8;

	@Override
	public void execute(CommandSender sender, Set<String> flags, String... args) {
		StatType type = StatsManager.getInstance().getType(args[0]);
		if (type == null) {
			sender.sendMessage(
					ChatColor.RED + "'" + args[0] + "' is not a valid stat name! type /s to see a list of stats");
			return;
		}

		int pageNumber = 0;
		if (args.length == 2)
			try {
				pageNumber = Integer.parseInt(args[1]) - 1;
			}
			catch (NumberFormatException e) {}

		// update
		if (!lastUpdates.containsKey(type) || lastUpdates.get(type) < System.currentTimeMillis() - timeBetweenUpdates) {
			final int pageNumberFinal = pageNumber;
			Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
				updateStat((Player) sender, type);
				showStat(sender, type, pageNumberFinal);
			});
		}
		else
			showStat(sender, type, pageNumber);
	}

	private void updateStat(Player sender, StatType type) {
		Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
			int entries = StatsDatabase.getInstance().countEntries(GCStatsConfig.serverName);
			if (entries > 1000)
				sender.sendMessage(ChatColor.GOLD + "Sorting " + ChatColor.AQUA + entries + ChatColor.GOLD
						+ " entries, please wait...");
		});

		ExecutorService es = Executors.newCachedThreadPool();

		for (Player player : Bukkit.getOnlinePlayers())
			es.execute(() -> {
				StatsDatabase.getInstance().setStatSync(player, type, type.get(player));
			});

		es.shutdown();
		try {
			es.awaitTermination(5, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

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

	void showStat(CommandSender sender, StatType type, int pageNumber) {
		if (pageNumber < 0)
			pageNumber = 0;

		int maxPage = Math.max(0, playersSorted.get(type).size() / playersPerPage);
		if (pageNumber > maxPage)
			pageNumber = maxPage;

		int min = pageNumber * playersPerPage;
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
			messages[1] = ChatColor.AQUA + "[" + (rank + 1) + "] " + ChatColor.GREEN + name + ChatColor.WHITE + "  "
					+ value;
		}

		messages[0] = ChatColor.GRAY + " ============[ " + ChatColor.DARK_AQUA + "Top " + type.getName()
				+ (pageNumber == 0 ? "" : ", " + ChatColor.GREEN + "Page " + (pageNumber + 1) + " / " + (maxPage + 1))
				+ ChatColor.GRAY + " ]============";

		messages[messages.length - 1] = ChatColor.GRAY + StringUtils.repeat("=", messages[0].length() - 9);

		for (int i = min; i <= max; i++) {
			String player = playersSorted.get(type).get(i);
			String value = type.valueToString(playerToStat.get(type).get(player));
			messages[i - min + offset] = ChatColor.GOLD + "[" + (i + 1) + "] " + ChatColor.GREEN + player
					+ ChatColor.WHITE + "  " + value;
		}

		sender.sendMessage(messages);
	}
}
