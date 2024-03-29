
package jdz.statsTracker.stats.defaultTypes;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import jdz.statsTracker.GCStats;
import jdz.statsTracker.stats.abstractTypes.BufferedStatType;
import lombok.Getter;

public class StatTypePlayTime extends BufferedStatType {
	@Getter private static final StatTypePlayTime instance = new StatTypePlayTime();
	
	public static void init() {
		Bukkit.getScheduler().runTaskTimer(GCStats.getInstance(), ()->{
			for (Player player : Bukkit.getOnlinePlayers())
				instance.updateTime(player);
		}, 600, 600);
		
	}

	public StatTypePlayTime() {
		super();
	}

	@Override
	public String getName() {
		return "Play time";
	}

	@Override
	public String valueToString(double value) {
		return timeFromSeconds((int) value);
	}

	public Map<Player, Long> lastTime = new HashMap<>();
	private Map<Player, Location> lastLocation = new HashMap<>();
	private Map<Player, Long> afkTime = new HashMap<>();

	@Override
	public void addPlayer(Player player, double value) {
		lastTime.put(player, System.currentTimeMillis() / 1000L);
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
		long time = System.currentTimeMillis() / 1000L;
		long timeDiff = time - lastTime.get(player);

		if (lastLocation.get(player).equals(player.getLocation()))
			afkTime.put(player, afkTime.get(player) + timeDiff);
		else {
			afkTime.put(player, 0L);
			lastLocation.put(player, player.getLocation());
		}

		if (afkTime.get(player) < 60)
			set(player, timeDiff + super.get(player));

		lastTime.put(player, time);
	}

	private String timeFromSeconds(int totalSeconds) {
		int days = totalSeconds / 86400;
		int hours = totalSeconds % 86400 / 3600;
		int minutes = totalSeconds % 86400 % 3600 / 60;
		int seconds = totalSeconds % 86400 % 3600 % 60;

		String rs = "";
		if (days > 0)
			rs = rs + days + "d ";
		if (hours > 0)
			rs = rs + hours + "h ";
		if (minutes > 0)
			rs = rs + minutes + "m ";
		if (seconds > 0)
			rs = rs + seconds + "s ";

		if (rs.equals(""))
			rs = "0s";
		else
			rs = rs.substring(0, rs.length() - 1);

		return rs;
	}
}
