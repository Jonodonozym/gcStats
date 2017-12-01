 
package jdz.statsTracker.stats;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import jdz.bukkitUtils.misc.TimedTask;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.config.Config;

public class PlayTimeRecorder{
	private static PlayTimeRecorder instance;
	
	public static PlayTimeRecorder getInstance() {
		return instance;
	}
	
	public static PlayTimeRecorder init(JavaPlugin plugin) {
		instance = new PlayTimeRecorder(plugin);
		return instance;
	}

	private static final int MAX_STRIKES = 4;
	
	public final Map<Player, Long> lastTime = new HashMap<Player, Long>();
	private final Map<Player,Location> lastLocation = new HashMap<Player,Location>();
	private final Map<Player,Integer> strikes = new HashMap<Player,Integer>();
	private final TimedTask task;

	public void addPlayer(Player p){
		lastTime.put(p, System.currentTimeMillis());
		lastLocation.put(p, p.getLocation());
		strikes.put(p, 0);
	}
	
	public void removePlayer(Player p){
		lastTime.remove(p);
		lastLocation.remove(p);
		strikes.remove(p);
	}
	
	public PlayTimeRecorder(JavaPlugin plugin) {
		task = new TimedTask(plugin, Config.afkTime/MAX_STRIKES, ()->{
			if (StatsDatabase.getInstance().getApi().isConnected() && Config.enabledStats.contains(StatType.PLAY_TIME))
				for (Player p: GCStats.plugin.getServer().getOnlinePlayers()){
					
					// recording if afk
					if (!lastLocation.containsKey(p))
						lastLocation.put(p, new Location(GCStats.plugin.getServer().getWorlds().get(0), 0, 0, 0));
					if (lastLocation.get(p).equals(p.getLocation()))
						strikes.put(p, strikes.get(p)+1);
					else
						strikes.put(p, 0);
					
					// if not afk, then updates time
					updateTime(p);
				}
		});
		task.start();
	}
	
	public void updateTime(Player p){
		if (lastTime.get(p) == null){
			lastTime.put(p, System.currentTimeMillis());
			return;
		}
		
		if (strikes.get(p) < MAX_STRIKES){
			long time = System.currentTimeMillis();
			if (StatsDatabase.getInstance().getApi().isConnected()){
				if (!isAfk(p))
					StatsDatabase.getInstance().addStat(p, StatType.PLAY_TIME, 
							(time-lastTime.get(p))/1000);
				AchievementData.updateAchievements(p, StatType.PLAY_TIME);
			}
			lastTime.put(p, time);
		}
	}
	
	public boolean isAfk(Player p){
		return strikes.get(p) >= MAX_STRIKES;
	}
}
