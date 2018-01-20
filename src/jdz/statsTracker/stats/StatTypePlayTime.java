
package jdz.statsTracker.stats;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import jdz.bukkitUtils.misc.TimedTask;
import jdz.statsTracker.GCStatsTracker;
import lombok.Getter;

class StatTypePlayTime extends BufferedStatType {
	@Getter private static final StatTypePlayTime instance = new StatTypePlayTime();
	
	public StatTypePlayTime() {
		super();
		new TimedTask(GCStatsTracker.instance, 600, ()->{
			for (Player player: Bukkit.getOnlinePlayers())
				updateTime(player);
		}).start();
	}

	@Override
	public String getName() {
		return "Play time";
	}

	@Override
	public String valueToString(double value) {
		return null;
	}

	public Map<Player, Long> lastTime = new HashMap<Player, Long>();
	private Map<Player,Location> lastLocation = new HashMap<Player,Location>();
	private Map<Player,Long> afkTime = new HashMap<Player,Long>();

	@Override
	public void addPlayer(Player player, double value) {
		lastTime.put(player, System.currentTimeMillis());
		lastLocation.put(player, player.getLocation());
		afkTime.put(player, 0L);
		super.addPlayer(player, value);
	}
	
	@Override
	public double removePlayer(Player player) {
		double value = super.removePlayer(player);
		lastTime.remove(player);
		lastLocation.remove(player);
		afkTime.remove(player);
		return value;
	}
	
	@Override
	public double get(Player player) {
		updateTime(player);
		return super.get(player);
	}
	
	private void updateTime(Player player) {
		long time = System.currentTimeMillis();
		long timeDiff = time = lastTime.get(player);

		if (lastLocation.get(player).equals(player.getLocation()))
			afkTime.put(player, afkTime.get(player)+timeDiff);
		else
			afkTime.put(player, 0L);
		
		if (afkTime.get(player) < 120000) 
			set(player, time - lastTime.get(player) + super.get(player));

		lastTime.put(player, time);
	}
}
