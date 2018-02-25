package jdz.statsTracker.achievement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsDatabase;
import jdz.statsTracker.stats.StatsManager;
import jdz.bukkitUtils.fileIO.FileLogger;
import jdz.statsTracker.GCStatsTracker;
import jdz.statsTracker.GCStatsTrackerConfig;

public class AchievementInventories implements Listener {
	public static final String SERVER_SELECT_INV_NAME = ChatColor.DARK_GREEN + "Achievements: server select";
	public static Inventory serverSelect;

	private static Map<String, List<RemoteAchievement>> allAchievements = new HashMap<String, List<RemoteAchievement>>();
	public static Map<RemoteAchievement, ItemStack> achievementToStack = new HashMap<RemoteAchievement, ItemStack>();

	public static Map<Player, OfflinePlayer> targets = new HashMap<Player, OfflinePlayer>();
	public static Map<Player, Integer> page = new HashMap<Player, Integer>();
	public static Map<Player, String> server = new HashMap<Player, String>();

	public static void reload() {
		targets.clear();

		List<String> servers = AchievementDatabase.getInstance().getServers();

		if (servers.size() > 0) {
			ExecutorService es = Executors.newFixedThreadPool(servers.size());
			for (String server : servers) {
				es.execute(() -> {
					List<RemoteAchievement> removeAchievements = AchievementDatabase.getInstance()
							.getServerAchievements(server);
					removeAchievements.sort((a, b) -> {
						return a.getName().compareTo(b.getName());
					});
					allAchievements.put(server, removeAchievements);
				});
			}

			es.shutdown();
			try {
				es.awaitTermination(1, TimeUnit.MINUTES);
			}
			catch (InterruptedException e) {
				new FileLogger(GCStatsTracker.instance).createErrorLog(e);
			}
		}

		achievementToStack = createDefaultItemStacks();
		serverSelect = createServerSelectInv();
	}

	public static void openServerSelect(Player player, OfflinePlayer otherPlayer) {
		player.openInventory(serverSelect);
		targets.put(player, otherPlayer);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		Inventory inv = e.getClickedInventory();

		if (inv == null || inv.getName() == null)
			return;

		ItemStack stack = null;
		if (e.getCurrentItem() != null)
			stack = e.getCurrentItem();
		else if (e.getCursor() != null)
			stack = e.getCursor();

		if (stack == null)
			return;

		if (inv.getName().equals(SERVER_SELECT_INV_NAME)) {
			if (stack != null && stack.getItemMeta() != null && stack.getItemMeta().getDisplayName() != null) {
				server.put(player, stack.getItemMeta().getDisplayName().substring(2));
				openPage(player, server.get(player), 0);
			}
			e.setCancelled(true);
		}
		else if (inv.getName().endsWith(" Achievements")) {
			if (stack != null && stack.getItemMeta() != null && stack.getItemMeta().getDisplayName() != null) {
				if (stack.getItemMeta().getDisplayName().equals(ArrowType.NEXT.toString()))
					openPage(player, server.get(player), page.get(player) + 1);
				if (stack.getItemMeta().getDisplayName().equals(ArrowType.PREVIOUS.toString()))
					openPage(player, server.get(player), page.get(player) - 1);
				if (stack.getItemMeta().getDisplayName().equals(ArrowType.SELECT_SERVER.toString()))
					player.openInventory(serverSelect);
			}
			e.setCancelled(true);
		}
	}

	/**
	 * Creates a sever select inventory
	 * 
	 * @param p
	 * @return
	 */
	private static Inventory createServerSelectInv() {
		List<ItemStack> itemStacks = new ArrayList<ItemStack>();

		for (String server : AchievementDatabase.getInstance().getServers())
			itemStacks.add(AchievementDatabase.getInstance().getServerIcon(server));

		int rows = (int) (Math.ceil(itemStacks.size() / 4.0));
		Inventory inv = Bukkit.createInventory(null, rows * 9, SERVER_SELECT_INV_NAME);

		int index = 0;
		for (ItemStack itemStack : itemStacks) {
			int location = index * 2 + 1 + (index / 4);
			if (rows - 1 <= index / 4)
				switch (itemStacks.size() % 4) {
				case 1:
					location = (rows - 1) * 9 + 4;
					break;
				case 2:
					location = (rows - 1) * 9 + 3 + (index % 4) * 2;
					break;
				case 3:
					location = (rows - 1) * 9 + 2 + (index % 4) * 2;
					break;
				}
			inv.setItem(location, itemStack);
			index++;
		}

		return inv;
	}

	private static Map<RemoteAchievement, ItemStack> createDefaultItemStacks() {
		Map<RemoteAchievement, ItemStack> achToItem = new HashMap<RemoteAchievement, ItemStack>();
		for (List<RemoteAchievement> list : allAchievements.values())
			for (RemoteAchievement a : list) {
				ItemStack itemStack = new ItemStack(a.getIcon(), 1, a.getIconDamage());
				achToItem.put(a, itemStack);
			}
		return achToItem;
	}

	private static void openPage(Player p, String server, int number) {
		p.openInventory(getPageInventory(targets.get(p), server, number));
		page.put(p, number);
	}

	private static Inventory getPageInventory(OfflinePlayer offlinePlayer, String server, int page) {
		List<RemoteAchievement> achievements = allAchievements.get(server);
		Inventory pageInventory = Bukkit.createInventory(null, 54,
				ChatColor.DARK_GREEN + offlinePlayer.getName() + ChatColor.DARK_GREEN + "'s Achievements");

		int i = 0;
		for (int achIndex = page * 36; achIndex < Math.min((page + 1) * 36, achievements.size()); achIndex++) {
			RemoteAchievement achievement = achievements.get(achIndex);
			final int f = i;
			new BukkitRunnable() {
				@Override
				public void run() {
					ItemStack itemStack = getPlayerStack(offlinePlayer, achievement);

					pageInventory.setItem(f, itemStack);
				}
			}.runTaskAsynchronously(GCStatsTracker.instance);
			i++;
		}

		if ((page + 1) * 36 < achievements.size())
			pageInventory.setItem(53, new ChangePageArrow(ArrowType.NEXT));
		if (page > 0)
			pageInventory.setItem(45, new ChangePageArrow(ArrowType.PREVIOUS));
		pageInventory.setItem(49, new ChangePageArrow(ArrowType.SELECT_SERVER));

		return pageInventory;
	}

	private static ItemStack getPlayerStack(OfflinePlayer offlinePlayer, RemoteAchievement achievement) {
		boolean isAchieved = AchievementDatabase.getInstance().isAchieved(offlinePlayer, achievement,
				achievement.getServer());
		ItemStack newStack = new ItemStack(achievementToStack.get(achievement));
		ItemMeta itemMeta = newStack.getItemMeta();

		List<String> lore = new ArrayList<String>();

		lore.add(ChatColor.YELLOW + achievement.getDescription());

		if (isAchieved) {
			if (!achievement.getRewardText().equals(""))
				lore.add(ChatColor.GREEN + "Reward: " + ChatColor.WHITE + achievement.getRewardText());
			lore.add("");

			itemMeta.setDisplayName(ChatColor.GREEN + achievement.getName().replace('_', ' '));
			itemMeta.addEnchant(Enchantment.DURABILITY, 10, true);
			lore.add(ChatColor.GREEN + "Achievement Unlocked!");
		}
		else {
			itemMeta.setDisplayName(ChatColor.RED + achievement.getName().replace('_', ' '));
			if (achievement.isHidden()) {
				lore.clear();
				lore.add(ChatColor.GRAY + achievement.getDescription().replaceAll("[^\\s]", "?"));
				if (!achievement.getRewardText().equals(""))
					lore.add(ChatColor.GREEN + "Reward: " + ChatColor.GRAY + ChatColor.ITALIC
							+ achievement.getRewardText().replaceAll("[^\\s]", "?"));
				newStack.setType(Material.BEDROCK);
			}
			else {
				if (!achievement.getRewardText().equals(""))
					lore.add(ChatColor.GREEN + "Reward: " + ChatColor.GRAY + ChatColor.ITALIC
							+ achievement.getRewardText());
				lore.add("");
				if (achievement instanceof RemoteStatAchievement) {
					StatType type = StatsManager.getInstance()
							.getType(((RemoteStatAchievement) achievement).getStatTypeName());
					double progress;
					if (achievement.getServer().replaceAll("_", " ").equalsIgnoreCase(GCStatsTrackerConfig.serverName)
							&& offlinePlayer.isOnline())
						progress = type.get(offlinePlayer.getPlayer());
					else
						progress = StatsDatabase.getInstance().getStat(offlinePlayer,
								((RemoteStatAchievement) achievement).getStatTypeName(), achievement.getServer());
					double required = ((RemoteStatAchievement) achievement).getRequirement();
					if (type == null)
						lore.add("" + ChatColor.GRAY + ChatColor.ITALIC + progress + " / " + required);
					else {
						String progressStr = type.valueToString(progress);
						String requiredStr = type.valueToString(required);
						lore.add("" + ChatColor.GRAY + ChatColor.ITALIC + progressStr + " / " + requiredStr);
					}
				}
			}
		}
		itemMeta.setLore(lore);
		newStack.setItemMeta(itemMeta);
		return newStack;
	}

	private static class ChangePageArrow extends ItemStack {

		public ChangePageArrow(ArrowType type) {
			super(Material.ARROW);
			ItemMeta im = getItemMeta();
			im.setDisplayName(type.toString());
			setItemMeta(im);
			setType(Material.ARROW);
		}
	}

	private static enum ArrowType {
		SELECT_SERVER, PREVIOUS, NEXT;

		@Override
		public String toString() {
			switch (this) {
			case NEXT:
				return ChatColor.GREEN + "Next Page";
			case PREVIOUS:
				return ChatColor.GREEN + "Previous Page";
			case SELECT_SERVER:
				return ChatColor.GREEN + "Select Server";
			}
			return "";
		}
	}
}
