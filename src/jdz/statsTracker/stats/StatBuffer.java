
package jdz.statsTracker.stats;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

import jdz.bukkitUtils.misc.TimedTask;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.config.Config;
public class StatBuffer {
	private static Map<StatType, TimedTask> tasks = new HashMap<StatType, TimedTask>();
	private static Map<Player, Map<StatType, Double>> bufferedStats = new HashMap<Player, Map<StatType, Double>>();
	private static Map<Player, Set<StatType>> hasChanged = new HashMap<Player, Set<StatType>>();
	
	public static void addType(StatType type){
		if (Config.enabledStats.contains(type))
			if (!tasks.containsKey(type)){
				tasks.put(type, new TimedTask(GCStats.plugin, Config.autoUpdateDelay, ()->{
					if (StatsDatabase.getInstance().getApi().isConnected())
						for(Player p: GCStats.plugin.getServer().getOnlinePlayers()){
							if (hasChanged.get(p).contains(type)){
								StatsDatabase.getInstance().setStat(p, type, bufferedStats.get(p).get(type));
								AchievementData.updateAchievements(p, type);
								hasChanged.get(p).remove(type);
							}
						}
				}));
				tasks.get(type).start();
			}
	}
	
	public static boolean containsType(StatType type) {
		return tasks.containsKey(type);
	}
	
	public static void addStat(Player player, StatType type, double amount){
		if (tasks.containsKey(type)){
			hasChanged.get(player).add(type);
			bufferedStats.get(player).put(type, amount+bufferedStats.get(player).get(type));
		}
	}
	
	public static void setStat(Player player, StatType type, double amount){
		if (tasks.containsKey(type)){
			hasChanged.get(player).add(type);
			bufferedStats.get(player).put(type, amount);
		}
	}
	
	public static void addPlayer(Player player){
		Map<StatType, Double> map = new HashMap<StatType, Double>();
		for (StatType s: tasks.keySet())
			map.put(s, StatsDatabase.getInstance().getStat(player, s.toString()));
		bufferedStats.put(player, map);
		hasChanged.put(player, new HashSet<StatType>());
	}
	
	public static void removePlayer(Player player){
		for(StatType s: tasks.keySet())
			StatsDatabase.getInstance().setStat(player, s, bufferedStats.get(player).get(s));
		hasChanged.remove(player);
		bufferedStats.remove(player);
	}
	
}
