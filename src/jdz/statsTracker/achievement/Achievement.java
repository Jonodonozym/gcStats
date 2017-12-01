
package jdz.statsTracker.achievement;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import jdz.statsTracker.GCStats;
import jdz.statsTracker.config.Config;

public class Achievement {
	// static field for the firework effect
	private static final FireworkEffect fwe;
	static {
		List<Color> c = new ArrayList<Color>();
		c.add(Color.LIME);
		fwe = FireworkEffect.builder().flicker(true).withColor(c).withFade(c).with(Type.BALL_LARGE).trail(true).build();
	}

	public final String name;
	public final String statType;
	public final double required;
	public final int points;
	public final Material icon;
	public final short iconDamage;
	public final String description;
	public final String server;

	public Achievement(String name, String statType, double required, int points, Material m, short iconDamage, String description,
			String server) {
		this.name = name;
		this.statType = statType;
		this.required = required;
		this.icon = m;
		this.iconDamage = iconDamage;
		this.description = description;
		this.points = points;
		this.server = server;
	}

	public boolean isAchieved(double stat) {
		return (stat >= required);
	}

	/**
	 * Shoots off a firework above a player's head
	 * 
	 * @param p
	 */
	public void doFirework(Player p) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (Config.achievementFireworkEnabled){
					Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), org.bukkit.entity.EntityType.FIREWORK);
					FireworkMeta fwm = fw.getFireworkMeta();
					fwm.addEffect(fwe);
					fwm.setPower(2);
					fw.setFireworkMeta(fwm);
				}

				if (Config.achievementMessageEnabled) {
					p.sendMessage(ChatColor.GREEN + "Achievement '" + name.replace('_', ' ') + "' Unlocked!");
					p.sendMessage(ChatColor.GREEN + "Reward: " + points + " points");
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 10, 1);
				}
			}
		}.runTask(GCStats.plugin);
	}
}
