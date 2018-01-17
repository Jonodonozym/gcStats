 
package jdz.statsTracker.stats;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import jdz.bukkitUtils.misc.TimedTask;
import jdz.statsTracker.GCStatsTracker;
import jdz.statsTracker.GCStatsTrackerConfig;
import jdz.statsTracker.achievement.AchievementData;

public class PlayTimeRecorder implements Listener{
	private static final PlayTimeRecorder instance = new PlayTimeRecorder();
	public static PlayTimeRecorder getInstance() {return instance;}

	private final int maxStrikes = 4;
	
	public Map<Player, Long> lastTime = new HashMap<Player, Long>();
	private Map<Player,Location> lastLocation = new HashMap<Player,Location>();
	private Map<Player,Integer> strikes = new HashMap<Player,Integer>();
	
	public final TimedTask task;
	
	private PlayTimeRecorder() {
		task = new TimedTask(GCStatsTracker.instance, GCStatsTrackerConfig.afkTime/maxStrikes, ()->{
			if (GCStatsTrackerConfig.enabledStats.contains(StatType.PLAY_TIME))
				for (Player p: GCStatsTracker.instance.getServer().getOnlinePlayers()){
					
					// checking if afk
					if (!lastLocation.containsKey(p))
						lastLocation.put(p, new Location(GCStatsTracker.instance.getServer().getWorlds().get(0), 0, 0, 0));
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
		
		if (strikes.get(p) < maxStrikes){
			long time = System.currentTimeMillis();
			if (StatsDatabase.getInstance().isConnected()){
				if (!isAfk(p))
					StatsDatabase.getInstance().addStat(p, StatType.PLAY_TIME, 
							(time-lastTime.get(p))/1000);
				AchievementData.checkAchievements(p, StatType.PLAY_TIME);
			}
			lastTime.put(p, time);
		}
	}
	
	public boolean isAfk(Player p){
		return strikes.get(p) >= maxStrikes;
	}


	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent e){
		onPlayerJoin(e.getPlayer());
	}
	
	public void onPlayerJoin(Player p) {
		lastTime.put(p, System.currentTimeMillis());
		lastLocation.put(p, p.getLocation());
		strikes.put(p, 0);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent e){
		onPlayerQuit(e.getPlayer());
	}
	
	public void onPlayerQuit(Player p) {
		lastTime.remove(p);
		lastLocation.remove(p);
		strikes.remove(p);
	}
}
