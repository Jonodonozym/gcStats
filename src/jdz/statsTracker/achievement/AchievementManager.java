
package jdz.statsTracker.achievement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import jdz.bukkitUtils.components.RomanNumber;
import jdz.bukkitUtils.fileIO.FileLogger;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.achievement.achievementTypes.RemoteAchievement;
import jdz.statsTracker.achievement.achievementTypes.StatAchievement;
import jdz.statsTracker.achievement.database.AchievementDatabase;
import jdz.statsTracker.event.AchievementUnlockEvent;
import jdz.statsTracker.event.StatChangeEvent;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsManager;
import lombok.Getter;

public class AchievementManager implements Listener {
	@Getter public static final AchievementManager instance = new AchievementManager();

	@Getter private final List<Achievement> achievements = new ArrayList<>();
	private final Map<String, Achievement> achievementsByName = new HashMap<>();
	private final Map<StatType, Set<StatAchievement>> achievementsByType = new HashMap<>();
	private final Map<Plugin, List<Achievement>> achievementsByPlugin = new HashMap<>();

	private final Map<Player, Set<Achievement>> localEarntAchievements = new HashMap<>();
	private final Map<Player, Integer> achievementPoints = new HashMap<>();
	private final Set<Player> areLoaded = new HashSet<>();

	public void addAchievements(Plugin plugin, Achievement... achievements) {
		if (achievements == null || achievements.length == 0)
			return;

		List<Achievement> added = new ArrayList<>();

		if (!achievementsByPlugin.containsKey(plugin))
			achievementsByPlugin.put(plugin, new ArrayList<Achievement>());

		for (Achievement achievement : achievements) {
			if (achievement == null)
				continue;

			if (achievementsByName.containsKey(achievement.getName())) {
				GCStats.getInstance().getLogger().warning("Achievement '" + achievement.getName()
						+ "' has a conflicting name with an existing achievement, skipping");
				continue;
			}

			achievementsByName.put(achievement.getName(), achievement);
			this.achievements.add(achievement);
			added.add(achievement);

			if (achievement instanceof StatAchievement) {
				StatType type = ((StatAchievement) achievement).getStatType();
				if (!achievementsByType.containsKey(type))
					achievementsByType.put(type, new HashSet<StatAchievement>());
				achievementsByType.get(type).add((StatAchievement) achievement);
			}
			achievementsByPlugin.get(plugin).add(achievement);
		}

		if (added.isEmpty())
			return;

		for (Player player : Bukkit.getOnlinePlayers())
			loadAchievements(player, Arrays.asList(achievements));
		AchievementDatabase.getInstance().addAchievements(added.toArray(new Achievement[added.size()]));
		AchievementInventories.getInstance().updateLocalAchievements();
	}

	public void removeAchievements(Achievement... achievements) {
		if (achievements == null || achievements.length == 0)
			return;

		this.achievements.removeAll(Arrays.asList(achievements));
		for (Achievement achievement : achievements) {
			if (achievement == null)
				continue;
			achievementsByName.remove(achievement.getName());
			if (achievement instanceof StatAchievement) {
				StatType type = ((StatAchievement) achievement).getStatType();
				achievementsByType.get(type).remove(achievement);
			}
		}

		for (Player player : localEarntAchievements.keySet())
			localEarntAchievements.get(player).removeAll(Arrays.asList(achievements));
		AchievementInventories.getInstance().updateLocalAchievements();
	}

	public boolean isAchieved(OfflinePlayer player, Achievement achievement) {
		String server = achievement instanceof RemoteAchievement ? ((RemoteAchievement) achievement).getServer()
				: GCStatsConfig.serverName;

		if (player.isOnline() && server.equals(GCStatsConfig.serverName)) {
			Achievement localAchievement = achievementsByName.get(achievement.getName());
			if (localAchievement != null)
				return localEarntAchievements.get(player.getPlayer()).contains(localAchievement);
		}
		return AchievementDatabase.getInstance().isAchieved(player, achievement, server);
	}

	public void setAchieved(Player player, Achievement achievement) {
		for (Achievement a : achievement.getPreRequisites())
			if (!isAchieved(player, a))
				return;

		localEarntAchievements.get(player).add(achievement);

		if (AchievementConfig.isFireworkEnabled())
			achievement.doFirework(player);
		if (AchievementConfig.isMessageEnabled())
			achievement.doMessages(player);
		if (AchievementConfig.isGiveRewards())
			achievement.giveRewards(player);

		Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
			AchievementDatabase.getInstance().setAchieved(player, achievement);
		});

		GCStats.getFileLogger().log(player.getName() + " unlocked the " + achievement.getName() + " achievement");
		new AchievementUnlockEvent(achievement, player).call();
	}

	public int getAchievementPoints(OfflinePlayer player) {
		if (player.isOnline())
			return achievementPoints.get(player.getPlayer());
		return AchievementDatabase.getInstance().getAchievementPoints(player);
	}

	public void setAchievementPoints(Player player, int value) {
		if (player.isOnline())
			achievementPoints.put(player, value);
		AchievementDatabase.getInstance().setAchievementPoints(player, value);
	}

	public void addAchievementPoints(Player player, int change) {
		setAchievementPoints(player, change + getAchievementPoints(player));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onUnload(PluginDisableEvent event) {
		if (event.getPlugin().equals(GCStats.getInstance()))
			return;

		if (!achievementsByPlugin.containsKey(event.getPlugin()))
			return;
		List<Achievement> achievements = achievementsByPlugin.remove(event.getPlugin());
		removeAchievements(achievements.toArray(new Achievement[achievements.size()]));
		GCStats.getInstance().getLogger().info(achievements.size() + " Achievements unregistered");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		loadAchievements(e.getPlayer(), achievements);

		Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
			achievementPoints.put(player, AchievementDatabase.getInstance().getAchievementPoints(player));
		});
	}


	private void loadAchievements(Player player, List<Achievement> achievements) {
		if (!localEarntAchievements.containsKey(player))
			localEarntAchievements.put(player, new HashSet<Achievement>());

		if (achievements.isEmpty()) {
			areLoaded.add(player);
			return;
		}

		Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
			ExecutorService es = Executors.newFixedThreadPool(achievements.size());
			for (Achievement a : achievements)
				es.execute(() -> {
					if (AchievementDatabase.getInstance().isAchieved(player, a))
						localEarntAchievements.get(player).add(a);
				});
			es.shutdown();
			try {
				es.awaitTermination(10, TimeUnit.MINUTES);
			}
			catch (InterruptedException e) {}
			areLoaded.add(player);
		});
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		localEarntAchievements.remove(e.getPlayer());
		areLoaded.remove(e.getPlayer());
	}

	@EventHandler
	public void onStatChange(StatChangeEvent e) {
		if (!areLoaded.contains(e.getPlayer()))
			return;

		if (achievementsByType.containsKey(e.getType()))
			for (StatAchievement a : achievementsByType.get(e.getType()))
				if (!isAchieved(e.getPlayer(), a) && e.getNewValue() >= a.getRequired())
					setAchieved(e.getPlayer(), a);
	}

	public void addFromConfig(Plugin plugin, FileConfiguration achConfig) {
		addAchievements(plugin, getFromConfig(achConfig).toArray(new Achievement[1]));
	}

	public List<Achievement> getFromConfig(FileConfiguration achConfig) {
		List<Achievement> addedAchievements = new ArrayList<>();

		if (achConfig.contains("achievements"))
			for (String achievement : achConfig.getConfigurationSection("achievements").getKeys(false))
				try {
					String typeName = achConfig.getString("achievements." + achievement + ".type");
					StatType type = StatsManager.getInstance().getType(typeName);

					if (!StatsManager.getInstance().enabledStats().contains(type)) {
						GCStats.getInstance().getLogger().info("achievement " + achievement + " stat type '" + typeName
								+ "' is disabled in config.yml, skipping...");
						continue;
					}

					String description = achConfig.getString("achievements." + achievement + ".description");

					Material m = Material.GRASS;
					try {
						m = Material.valueOf(achConfig.getString("achievements." + achievement + ".icon"));
					}
					catch (Exception e) {}
					short iconDamage = (short) achConfig.getInt("achievements." + achievement + ".iconDamage", 0);

					List<Double> required = achConfig.getDoubleList("achievements." + achievement + ".required");
					List<Integer> points = achConfig.getIntegerList("achievements." + achievement + ".points");
					List<String> rewardText = achConfig.getStringList("achievements." + achievement + ".rewardText");

					if (required == null || required.isEmpty()) {
						required = new ArrayList<>();
						required.add(achConfig.getDouble("achievements." + achievement + ".required"));
					}
					if (points == null || points.isEmpty()) {
						points = new ArrayList<>();
						points.add(achConfig.getInt("achievements." + achievement + ".points"));
					}
					if (rewardText == null || rewardText.isEmpty()) {
						rewardText = new ArrayList<>();
						rewardText.add(achConfig.getString("achievements." + achievement + ".rewardText"));
					}
					while (points.size() < required.size())
						points.add(0);
					while (rewardText.size() < required.size())
						rewardText.add("");

					boolean hidden = achConfig.getBoolean("achievements." + achievement + ".hidden", false);
					boolean newLineBefore = achConfig.getBoolean("achievements." + achievement + ".newLineBefore",
							false);
					boolean newLineAfter = achConfig.getBoolean("achievements." + achievement + ".newLineAfter", false);

					boolean isTieredQuantity = achConfig.getBoolean("achievements." + achievement + ".iconQuantity",
							false);

					for (int i = 0; i < required.size(); i++) {
						List<String> commands = achConfig
								.getStringList("achievements." + achievement + ".rewardCommands." + (i + 1));
						List<String> messages = achConfig
								.getStringList("achievements." + achievement + ".rewardMessages." + (i + 1));

						String name = achievement + (required.size() == 1 ? "" : " " + RomanNumber.of(i + 1));
						Achievement ach = new StatAchievement(name, type, required.get(i), m, iconDamage,
								description.replaceAll("%required%", type.valueToString(required.get(i)))
										.replaceAll("\\{required\\}", type.valueToString(required.get(i))),
								points.get(i), rewardText.get(i), hidden);

						if (i == 0)
							ach.setNewLineBefore(newLineBefore);

						if (i == required.size() - 1)
							ach.setNewLineAfter(newLineAfter);

						if (commands != null)
							ach.setRewardCommands(commands);

						if (messages != null)
							ach.setRewardMessages(messages);

						if (isTieredQuantity)
							ach.setIconQuantity(i + 1);

						addedAchievements.add(ach);
					}

				}
				catch (Exception e) {
					GCStats.getInstance().getLogger().info("achievement " + achievement
							+ " has invalid configuration, check the error log for details");
					new FileLogger(GCStats.getInstance()).createErrorLog(e);
				}

		return addedAchievements;
	}
}
