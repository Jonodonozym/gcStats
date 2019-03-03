
package jdz.statsTracker.achievement;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;

import jdz.bukkitUtils.misc.StringUtils;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.achievement.database.AchievementDatabase;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class Achievement {
	// static field for the firework effect
	private static final FireworkEffect fwe;
	static {
		List<Color> c = new ArrayList<Color>();
		c.add(Color.LIME);
		fwe = FireworkEffect.builder().flicker(true).withColor(c).withFade(c).with(Type.BALL_LARGE).trail(true).build();
	}

	@NonNull @Getter private final String name;
	@NonNull @Getter private final Material icon;
	@Getter @Setter private int iconQuantity = 1;

	@Getter private final short iconDamage;
	@NonNull @Getter private final String[] description;
	@Getter private final int points;
	@NonNull @Getter private final String[] rewardText;
	@Getter private final boolean hidden;

	@Getter @Setter private boolean newLineBefore = false;
	@Getter @Setter private boolean newLineAfter = false;

	@Getter @Setter private boolean doFirework = true;

	@NonNull @Getter @Setter private List<String> rewardCommands = new ArrayList<String>();
	@NonNull @Getter @Setter private List<String> rewardMessages = new ArrayList<String>();

	public Achievement(String name, Material m, short iconDamage, String description) {
		this(name, m, iconDamage, description, 0, "", false);
	}

	public Achievement(String name, Material m, short iconDamage, String description, int points, String rewardText,
			boolean hidden) {
		this(name, m, iconDamage, StringUtils.splitIntoLines(description, 40), points,
				StringUtils.splitIntoLines(rewardText, 40), hidden);
	}

	public Achievement(String name, Material m, short iconDamage, String[] description, int points, String[] rewardText,
			boolean hidden) {
		this.name = name;
		this.icon = m;
		this.iconDamage = iconDamage;
		this.description = description;
		this.points = points;
		this.rewardText = new String[rewardText.length];
		for (int i = 0; i < rewardText.length; i++)
			this.rewardText[i] = (rewardText[i] == null || rewardText[i].equals("")
					? (points > 0 ? points + " Achievement point" + (points > 1 ? "s" : "") : "")
					: rewardText[i]).replaceAll("%points%", this.points + "").replaceAll("\\{points\\}",
							this.points + "");
		this.hidden = hidden;
	}

	/**
	 * Shoots off a firework above a player's head
	 * 
	 * @param p
	 */
	public void doFirework(Player p) {
		if (doFirework)
			Bukkit.getScheduler().runTask(GCStats.getInstance(), () -> {
				Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(),
						org.bukkit.entity.EntityType.FIREWORK);
				FireworkMeta fwm = fw.getFireworkMeta();
				fwm.addEffect(fwe);
				fwm.setPower(1);
				fw.setFireworkMeta(fwm);
			});
	}

	public void doMessages(Player p) {
		p.sendMessage(ChatColor.GREEN + "Achievement '" + name.replace('_', ' ') + "' Unlocked!");
		if (AchievementConfig.isGiveRewards()) {
			if (points > 0)
				p.sendMessage(ChatColor.GREEN + "Reward: " + points + " points");
			for (String s : rewardMessages)
				p.sendMessage(ChatColor.GREEN + s);
		}
		try {
			p.playSound(p.getLocation(), Sound.NOTE_PLING, 10, 1);
		}
		catch (NoSuchFieldError e) {}
	}

	public void giveRewards(Player p) {
		if (points > 0)
			AchievementDatabase.getInstance().addAchievementPoints(p, points);
		for (String command : rewardCommands)
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("\\{player\\}", p.getName()));
	}

	public boolean isAchieved(OfflinePlayer player) {
		return AchievementManager.getInstance().isAchieved(player, this);
	}

	public List<Achievement> getPreRequisites() {
		return new ArrayList<Achievement>();
	}
	
	public void register(Plugin plugin) {
		AchievementManager.getInstance().addAchievements(plugin, this);
	}
}
