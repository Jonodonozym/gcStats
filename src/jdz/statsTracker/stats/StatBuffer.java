
package jdz.statsTracker.stats;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.main.Config;
import jdz.statsTracker.main.Main;
import jdz.statsTracker.util.SqlApi;
import jdz.statsTracker.util.TimedTask;

public class StatBuffer {
	private static Map<StatType, TimedTask> tasks = new HashMap<StatType, TimedTask>();
	private static Map<Player, Map<StatType, Double>> bufferedStats = new HashMap<Player, Map<StatType, Double>>();
	private static Map<Player, Map<StatType, Boolean>> hasChanged = new HashMap<Player, Map<StatType, Boolean>>();
	
	public static void addType(StatType type){
		if (Config.enabledStats.contains(type))
			if (!tasks.containsKey(type)){
				tasks.put(type, new TimedTask(Config.autoUpdateDelay, ()->{
					if (SqlApi.isConnected())
						for(Player p: Main.plugin.getServer().getOnlinePlayers()){
							if (hasChanged.get(p).get(type)){
								SqlApi.setStat(p, StatType.BLOCKS_PLACED, bufferedStats.get(p).get(type));
								AchievementData.updateAchievements(p, type);
								hasChanged.get(p).put(type, false);
							}
						}
				}));
				tasks.get(type).start();
			}
	}
	
	public static void addStat(Player player, StatType type, double amount){
		if (tasks.containsKey(type)){
			hasChanged.get(player).put(type, true);
			bufferedStats.get(player).put(type, amount+bufferedStats.get(player).get(type));
		}
	}
	
	public static void addPlayer(Player player){
		Map<StatType, Double> map = new HashMap<StatType, Double>();
		for (StatType s: tasks.keySet())
			map.put(s, SqlApi.getStat(player, s.toString()));
		bufferedStats.put(player, map);
		Map<StatType, Boolean> map2 = new HashMap<StatType, Boolean>();
		for (StatType s: tasks.keySet())
			map2.put(s, false);
		hasChanged.put(player, map2);
	}
	
	public static void removePlayer(Player player){
		for(StatType s: tasks.keySet())
			SqlApi.setStat(player, s, bufferedStats.get(player).get(s));
		hasChanged.remove(player);
		bufferedStats.remove(player);
	}
	
}
