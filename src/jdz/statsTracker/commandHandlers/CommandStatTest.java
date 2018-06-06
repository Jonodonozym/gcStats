
package jdz.statsTracker.commandHandlers;

import java.util.HashMap;
import java.util.Map;
import jdz.bukkitUtils.misc.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import jdz.bukkitUtils.commands.SubCommand;
import jdz.bukkitUtils.commands.annotations.CommandLabel;
import jdz.bukkitUtils.commands.annotations.CommandOpOnly;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.stats.StatsManager;
import jdz.statsTracker.stats.StatsDatabase;
import jdz.statsTracker.stats.defaultTypes.StatTypeKills;

@CommandOpOnly
@CommandLabel("test")
public class CommandStatTest extends SubCommand {
	private static final int TEST_SIZE = 1000;

	@Override
	public void execute(CommandSender sender, String... args) {
		Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
			final Map<Player, Double> values = initValues(sender);
			Bukkit.getScheduler().runTaskLaterAsynchronously(GCStats.getInstance(), () -> {
				checkValues(sender, values);
			}, 20);
		});
	}

	private Map<Player, Double> initValues(CommandSender sender) {
		Map<Player, Double> kills = new HashMap<Player, Double>();
		ExecutorService es = Executors.newFixedThreadPool(TEST_SIZE);
		for (int i = 0; i < TEST_SIZE; i++) {
			final int i2 = i;
			es.execute(() -> {
				Player player = mockPlayer(i2 + "");
				StatsManager.getInstance().onPlayerJoin(new PlayerJoinEvent(player, ""));
				StatTypeKills.getInstance().set(player, Random.nextInt(10000));
				kills.put(player, StatTypeKills.getInstance().get(player));
				StatsManager.getInstance().onPlayerQuit(new PlayerQuitEvent(player, ""));
			});
		}
		es.shutdown();

		try {
			es.awaitTermination(30, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			sender.sendMessage(e.toString());
			e.printStackTrace();
		}
		return kills;
	}

	private void checkValues(CommandSender sender, Map<Player, Double> values) {
		ExecutorService es = Executors.newFixedThreadPool(values.size());
		int i = 0;
		for (Player player : values.keySet()) {
			final int name = i;
			es.execute(() -> {
				try {
					Thread.sleep(10000);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				double expected = values.get(player);
				double actual = StatsDatabase.getInstance().getStat(mockOfflinePlayer(name + ""),
						StatTypeKills.getInstance());
				if (expected != actual)
					sender.sendMessage(ChatColor.RED + player.getName() + " WRONG: " + expected + ", " + actual);
			});
		}
		es.shutdown();

		try {
			es.awaitTermination(30, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			sender.sendMessage(e.toString());
			e.printStackTrace();
		}
		
		sender.sendMessage("Test complete");
	}

	private Player mockPlayer(String name) {
		Player player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(UUID.randomUUID());
		when(player.getName()).thenReturn(name);
		return player;
	}

	private OfflinePlayer mockOfflinePlayer(String name) {
		OfflinePlayer player = mock(OfflinePlayer.class);
		when(player.getUniqueId()).thenReturn(UUID.randomUUID());
		when(player.getName()).thenReturn(name);
		return player;
	}

}
