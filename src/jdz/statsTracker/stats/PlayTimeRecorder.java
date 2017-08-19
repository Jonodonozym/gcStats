
package jdz.statsTracker.stats;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.main.Config;
import jdz.statsTracker.main.Main;
import jdz.statsTracker.util.SqlApi;
import jdz.statsTracker.util.TimedTask;

public class PlayTimeRecorder extends TimedTask{

	private static final int maxStrikes = 4;
	
	public static Map<Player, Long> lastTime = new HashMap<Player, Long>();
	private static Map<Player,Location> lastLocation = new HashMap<Player,Location>();
	private static Map<Player,Integer> strikes = new HashMap<Player,Integer>();
	public static TimedTask task;

	public PlayTimeRecorder() {
		super(Config.afkTime/maxStrikes, ()->{
			if (Config.statEnabled.get(StatType.PLAY_TIME_SECONDS))
				for (Player p: Main.plugin.getServer().getOnlinePlayers()){
					
					// recording if afk
					if (!lastLocation.containsKey(p))
						lastLocation.put(p, new Location(Main.plugin.getServer().getWorlds().get(0), 0, 0, 0));
					if (lastLocation.get(p).equals(p.getLocation()))
						strikes.put(p, strikes.get(p)+1);
					else
						strikes.put(p, 0);
					
					// if not afk, then updates time
					if (strikes.get(p) < maxStrikes){
						long time = System.currentTimeMillis();
						if (!PlayTimeRecorder.isAfk(p))
							SqlApi.addStat(Config.dbConnection, p, StatType.PLAY_TIME_SECONDS, 
									(lastTime.get(p) - time)/1000);
						AchievementData.updateAchievements(p, StatType.PLAY_TIME_SECONDS);
						lastTime.put(p, time);
					}
				}
		});
	}
	
	public static boolean isAfk(Player p){
		return strikes.get(p) >= maxStrikes;
	}
}
