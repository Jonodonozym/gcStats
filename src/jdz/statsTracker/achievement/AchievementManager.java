
package jdz.statsTracker.achievement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import jdz.statsTracker.stats.StatsManager;
import lombok.Getter;
import jdz.bukkitUtils.misc.Config;
import jdz.bukkitUtils.misc.RomanNumber;
import jdz.statsTracker.GCStatsTracker;
import jdz.statsTracker.GCStatsTrackerConfig;
import jdz.statsTracker.stats.StatChangeEvent;
import jdz.statsTracker.stats.StatType;

public class AchievementManager implements Listener{
	@Getter public static final AchievementManager instance = new AchievementManager();
	
	private Map<Player, Set<Achievement>> localEarntAchievements = new HashMap<Player, Set<Achievement>>();
	private Map<StatType, Set<StatAchievement>> achievementsByType = new HashMap<StatType, Set<StatAchievement>>();
	private Set<Achievement> achievements = new HashSet<Achievement>();
	private Map<Player, Integer> achievementPoints = new HashMap<Player, Integer>();
	
	public void addAchievements(Achievement...achievements) {
		this.achievements.addAll(Arrays.asList(achievements));
		for (Achievement achievement: achievements) {
			if (achievement instanceof StatAchievement) {
				StatType type = ((StatAchievement)achievement).getStatType();
				if (!achievementsByType.containsKey(type))
					achievementsByType.put(type, new HashSet<StatAchievement>());
				this.achievementsByType.get(type).add((StatAchievement)achievement);
			}
		}
		AchievementDatabase.getInstance().addAchievements(achievements);
	}
	
	public void removeAchievements(Achievement...achievements) {
		this.achievements.removeAll(Arrays.asList(achievements));
		for (Achievement achievement: achievements) {
			if (achievement instanceof StatAchievement) {
				StatType type = ((StatAchievement)achievement).getStatType();
				this.achievementsByType.get(type).remove(achievement);
			}
		}

		for (Player player: localEarntAchievements.keySet())
			localEarntAchievements.get(player).removeAll(Arrays.asList(achievements));
	}
	
	public boolean isAchieved(OfflinePlayer player, Achievement achievement) {
		if (player.isOnline())
			return localEarntAchievements.get(player.getPlayer()).contains(achievement);
		return AchievementDatabase.getInstance().isAchieved(player, achievement);
	}
	
	public void setAchieved(Player player, Achievement achievement) {
		localEarntAchievements.get(player).add(achievement);
		
		if (GCStatsTrackerConfig.achievementFireworkEnabled)
			achievement.doFirework(player);
		if (GCStatsTrackerConfig.achievementMessageEnabled)
			achievement.doMessages(player);
		if (GCStatsTrackerConfig.achievementGiveRewards)
			achievement.giveRewards(player);
		
		Bukkit.getScheduler().runTaskAsynchronously(GCStatsTracker.instance, ()->{
			AchievementDatabase.getInstance().setAchieved(player, achievement);
		});
		
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
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		addPlayer(e.getPlayer());
	}
	
	public void addPlayer(Player player) {
		localEarntAchievements.put(player, new HashSet<Achievement>());
		
		AchievementDatabase.getInstance().addPlayer(player);

		for (Achievement a: achievements)
			Bukkit.getScheduler().runTaskAsynchronously(GCStatsTracker.instance, () -> {
				if (AchievementDatabase.getInstance().isAchieved(player, a))
					localEarntAchievements.get(player).add(a);
			});
		
		Bukkit.getScheduler().runTaskAsynchronously(GCStatsTracker.instance, ()->{
			achievementPoints.put(player, AchievementDatabase.getInstance().getAchievementPoints(player));
		});
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		localEarntAchievements.remove(e.getPlayer());
	}
	
	@EventHandler
	public void onStatChange(StatChangeEvent e) {
		if (achievementsByType.containsKey(e.getType()))
			for (StatAchievement a: achievementsByType.get(e.getType()))
				if (!isAchieved(e.getPlayer(), a) && e.getNewValue() >= a.getRequired())
					setAchieved(e.getPlayer(), a);
	}
	
	public void reloadData() {
		FileConfiguration achConfig = Config.getConfig(GCStatsTracker.instance, "Achievements.yml");
		
		List<Achievement> localAchievements = new ArrayList<Achievement>();
		
		for (String achievement : achConfig.getConfigurationSection("achievements").getKeys(false)) {
			try {
				String typeName = achConfig.getString("achievements." + achievement + ".type");
				StatType type = StatsManager.getInstance().getType(typeName);
				
				if (!StatsManager.getInstance().enabledStats().contains(type)){
					GCStatsTracker.instance.getLogger().info("achievement " + achievement + " stat type '"+typeName+"' is disabled in config.yml, skipping...");
					continue;
				}
				
				String description = achConfig.getString("achievements." + achievement + ".description");
				
				Material m = Material.GRASS;
				try {
					m = Material.valueOf(achConfig.getString("achievements." + achievement + ".icon"));
				} catch (Exception e) {	}
				short iconDamage = (short)achConfig.getInt("achievements." + achievement + ".iconDamage");

				List<Double> required = achConfig.getDoubleList("achievements." + achievement + ".required");
				List<Integer> points = achConfig.getIntegerList("achievements." + achievement + ".points");
				if (required == null) {
					required = new ArrayList<Double>();
					points = new ArrayList<Integer>();
					required.add(achConfig.getDouble("achievements." + achievement + ".required"));
					points.add(achConfig.getInt("achievements." + achievement + ".points"));
				}

				for (int i = 0; i < required.size(); i++) {
					List<String> commands = achConfig.getStringList("achievements." + achievement + ".rewardCommands."+i);
					List<String> messages = achConfig.getStringList("achievements." + achievement + ".rewardMessages."+i);
					
					String name = achievement + (required.size() == 1 ? "" : " " + RomanNumber.of(i+1));
					Achievement ach = new StatAchievement(name, type, required.get(i), m, iconDamage,
							description.replaceAll("\\{required\\}", type.valueToString(required.get(i)) + ""));
					
					if (points != null && points.size() < i && points.get(i) != null)
						ach.setPoints(points.get(i));
					
					if (commands != null)
						ach.setRewardCommands(commands);
					
					if (messages != null)
						ach.setRewardMessages(messages);
					
					localAchievements.add(ach);
				}

			} catch (Exception e) {
				GCStatsTracker.instance.getLogger().info("achievement " + achievement + " has invalid configuration, skipping...");
			}
		}
		
		if (localAchievements.isEmpty())
			return;
		
		addAchievements(localAchievements.toArray(new Achievement[1]));
	}
}
