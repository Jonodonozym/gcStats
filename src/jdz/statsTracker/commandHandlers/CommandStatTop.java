
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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandRequiredArgs;
import jdz.bukkitUtils.commands.annotations.CommandShortDescription;
import jdz.bukkitUtils.commands.annotations.CommandUsage;
import jdz.bukkitUtils.misc.StringUtils;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsManager;
import jdz.statsTracker.database.StatsDatabase;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@CommandLabel("top")
@CommandRequiredArgs(1)
@CommandShortDescription("Displays the top scores for a specific stat")
@CommandUsage("top <statNoSpaces> [pageNumber]")
class CommandStatTop extends SubCommand {
	@Getter private static final CommandStatTop instance = new CommandStatTop();

	private CommandStatTop() {}

	private final Map<StatType, Long> lastUpdates = new HashMap<StatType, Long>();

	private final Map<StatType, List<OfflinePlayer>> playersSorted = new HashMap<StatType, List<OfflinePlayer>>();
	final Map<StatType, Map<OfflinePlayer, Integer>> playerToRank = new HashMap<StatType, Map<OfflinePlayer, Integer>>();
	private final Map<StatType, Map<OfflinePlayer, Double>> playerToStat = new HashMap<StatType, Map<OfflinePlayer, Double>>();

	private final long timeBetweenUpdates = 120000;
	final int playersPerPage = 8;

	@Override
	public void execute(CommandSender sender, Set<String> flags, String... args) {
		StatType type = StatsManager.getInstance().getType(args[0]);
		if (type == null) {
			sender.sendMessage(
					ChatColor.RED + "'" + args[0] + "' is not a valid stat name! type /gcs to see a list of stats");
			return;
		}

		int pageNumber = 0;
		if (args.length == 2)
			try {
				pageNumber = Integer.parseInt(args[1]) - 1;
			}
			catch (NumberFormatException e) {}

		// update
		if (!lastUpdates.containsKey(type) || lastUpdates.get(type) > System.currentTimeMillis() + timeBetweenUpdates) {
			final int pageNumberFinal = pageNumber;
			Bukkit.getScheduler().runTaskAsynchronously(GCStats.instance, () -> {
				sender.sendMessage(ChatColor.GOLD + "Sorting player stats, please wait...");
				updateStat(type);
				showStat(sender, type, pageNumberFinal);
			});
		}
		else
			showStat(sender, type, pageNumber);
	}

	@SuppressWarnings("deprecation")
	private void updateStat(StatType type) {
		ExecutorService es = Executors.newCachedThreadPool();

		for (Player player : Bukkit.getOnlinePlayers())
			es.execute(() -> {
				StatsDatabase.getInstance().setStatSync(player, type, type.get(player));
			});

		es.shutdown();
		try {
			es.awaitTermination(15, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

		Map<String, Double> rows = StatsDatabase.getInstance().getAllSorted(type);

		List<OfflinePlayer> players = new ArrayList<OfflinePlayer>();
		Map<OfflinePlayer, Integer> ranks = new HashMap<OfflinePlayer, Integer>();
		Map<OfflinePlayer, Double> stats = new HashMap<OfflinePlayer, Double>();

		int i = 0;
		for (Map.Entry<String, Double> row : rows.entrySet()) {
			OfflinePlayer player = Bukkit.getOfflinePlayer(row.getKey());
			players.add(player);
			ranks.put(player, i++);
			stats.put(player, row.getValue());
		}

		playersSorted.put(type, players);
		playerToRank.put(type, ranks);
		playerToStat.put(type, stats);
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

		messages[0] = ChatColor.GRAY + " ============[ " + ChatColor.DARK_AQUA + "Top " + type.getName()
				+ (pageNumber == 0 ? "" : ", " + ChatColor.GREEN + "Page " + (pageNumber + 1) + " / " + (maxPage + 1))
				+ ChatColor.GRAY + " ]============";

		messages[messages.length - 1] = ChatColor.GRAY + StringUtils.repeat("=", messages[0].length() - 8);

		for (int i = min; i <= max; i++) {
			OfflinePlayer player = playersSorted.get(type).get(i);
			String playerName = player.getName();
			if (player.isOnline())
				playerName = player.getPlayer().getDisplayName();
			messages[i - min + 1] = ChatColor.GOLD + "" + (i + 1) + ") " + ChatColor.GREEN + playerName
					+ ChatColor.WHITE + ", " + type.valueToString(playerToStat.get(type).get(player));
		}

		sender.sendMessage(messages);
	}
}
