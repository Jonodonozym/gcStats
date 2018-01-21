
package jdz.statsTracker.achievement;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import jdz.statsTracker.GCStatsTrackerConfig;
import lombok.Getter;
import lombok.Setter;

public abstract class Achievement {
	// static field for the firework effect
	private static final FireworkEffect fwe;
	static {
		List<Color> c = new ArrayList<Color>();
		c.add(Color.LIME);
		fwe = FireworkEffect.builder().flicker(true).withColor(c).withFade(c).with(Type.BALL_LARGE).trail(true).build();
	}

	@Getter private final String name;
	@Getter private final Material icon;
	@Getter private final short iconDamage;
	@Getter private final String description;

	@Getter @Setter private int points = 0;
	@Getter @Setter private List<String> rewardCommands = new ArrayList<String>();
	@Getter @Setter private List<String> rewardMessages = new ArrayList<String>();
	

	public Achievement(String name, Material m, short iconDamage, String description) {
		this.name = name;
		this.icon = m;
		this.iconDamage = iconDamage;
		this.description = description;
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
		if (GCStatsTrackerConfig.achievementGiveRewards) {
			if (points > 0)
				p.sendMessage(ChatColor.GREEN + "Reward: " + points + " points");
			for (String s: rewardMessages)
				p.sendMessage(ChatColor.GREEN + s);
		}
		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 10, 1);
	}
	
	public void giveRewards(Player p) {
		if (points > 0)
			AchievementDatabase.getInstance().addAchievementPoints(p, points);
		for (String command: rewardCommands)
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("\\{player\\}", p.getName()));
	}
}
