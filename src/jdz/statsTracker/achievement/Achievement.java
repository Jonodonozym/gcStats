
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

import lombok.Getter;

public abstract class Achievement {
	// static field for the firework effect
	private static final FireworkEffect fwe;
	static {
		List<Color> c = new ArrayList<Color>();
		c.add(Color.LIME);
		fwe = FireworkEffect.builder().flicker(true).withColor(c).withFade(c).with(Type.BALL_LARGE).trail(true).build();
	}

	@Getter private final String name;
	@Getter private final int points;
	@Getter private final Material icon;
	@Getter private final short iconDamage;
	@Getter private final String description;

	public Achievement(String name, int points, Material m, short iconDamage, String description) {
		this.name = name;
		this.icon = m;
		this.iconDamage = iconDamage;
		this.description = description;
		this.points = points;
	}

	/**
	 * Shoots off a firework above a player's head
	 * 
	 * @param p
	 */
	public void doFirework(Player p) {
		Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), org.bukkit.entity.EntityType.FIREWORK);
		FireworkMeta fwm = fw.getFireworkMeta();
		fwm.addEffect(fwe);
		fwm.setPower(1);
		fw.setFireworkMeta(fwm);
	}

	public void doMessages(Player p) {
		p.sendMessage(ChatColor.GREEN + "Achievement '" + name.replace('_', ' ') + "' Unlocked!");
		p.sendMessage(ChatColor.GREEN + "Reward: " + points + " points");
		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 10, 1);
	}
}
