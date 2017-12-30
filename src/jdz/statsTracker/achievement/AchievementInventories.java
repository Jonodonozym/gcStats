
package jdz.statsTracker.achievement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import jdz.statsTracker.main.Config;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.util.SqlApi;

public class AchievementInventories implements Listener {
	public static final String SERVER_SELECT_INV_NAME = ChatColor.DARK_GREEN + "Achievements: server select";
	public static Inventory serverSelect;

	public static Map<Achievement, ItemStack> achievementToStack = new HashMap<Achievement, ItemStack>();
	public static Map<Player, OfflinePlayer> targets = new HashMap<Player, OfflinePlayer>();
	public static Map<Player, Integer> page = new HashMap<Player, Integer>();
	public static Map<Player, String> server = new HashMap<Player, String>();

	public static void reload() {
		targets.clear();
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
		} else if (inv.getName().endsWith(" Achievements")) {
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

		for (String server : Config.servers)
			itemStacks.add(SqlApi.getServerIcon(server));

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

	private static Map<Achievement, ItemStack> createDefaultItemStacks() {
		Map<Achievement, ItemStack> achToItem = new HashMap<Achievement, ItemStack>();
		for (List<Achievement> list : AchievementData.achievements.values())
			for (Achievement a : list) {
				ItemStack itemStack = new ItemStack(a.icon, 1, a.iconDamage);
				ItemMeta itemMeta = itemStack.getItemMeta();

				List<String> lore = new ArrayList<String>();
				lore.add(ChatColor.YELLOW + a.description);
				lore.add("" + ChatColor.GRAY + ChatColor.ITALIC + a.points + " Achievement Points");
				lore.add("");

				itemMeta.setLore(lore);
				itemStack.setItemMeta(itemMeta);
				achToItem.put(a, itemStack);
			}
		return achToItem;
	}

	private static void openPage(Player p, String server, int number) {
		new BukkitRunnable() {
			@Override
			public void run() {
				p.openInventory(getPageInventory(targets.get(p), server, number));
				page.put(p, number);
			}
		};

	}

	private static Inventory getPageInventory(OfflinePlayer offlinePlayer, String server, int page) {
		List<Achievement> achievements = AchievementData.achievements.get(server);
		Inventory pageInventory = Bukkit.createInventory(null, 54,
				ChatColor.DARK_GREEN + offlinePlayer.getName() + ChatColor.DARK_GREEN + "'s Achievements");
		int i = 0;
		for (int achIndex = page * 36; achIndex < Math.min((page + 1) * 36, achievements.size()); achIndex++) {
			Achievement achievement = achievements.get(achIndex);

			ItemStack itemStack = getPlayerStack(offlinePlayer, achievement, server);

			pageInventory.setItem(i++, itemStack);
		}

		if ((page + 1) * 36 < achievements.size())
			pageInventory.setItem(53, new ChangePageArrow(ArrowType.NEXT));
		if (page > 0)
			pageInventory.setItem(45, new ChangePageArrow(ArrowType.PREVIOUS));
		pageInventory.setItem(49, new ChangePageArrow(ArrowType.SELECT_SERVER));

		return pageInventory;
	}

	private static ItemStack getPlayerStack(OfflinePlayer offlinePlayer, Achievement achievement, String server) {
		boolean isAchieved = SqlApi.isAchieved(offlinePlayer, achievement);
		ItemStack newStack = new ItemStack(achievementToStack.get(achievement));
		ItemMeta itemMeta = newStack.getItemMeta();
		List<String> lore = itemMeta.getLore();
		if (isAchieved) {
			itemMeta.setDisplayName(ChatColor.GREEN + achievement.name.replace('_', ' '));
			newStack.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
			lore.add(ChatColor.GREEN + "Achievement Unlocked!");
			lore.get(1).replaceAll(ChatColor.GRAY.toString(), ChatColor.WHITE.toString());
		} else {
			itemMeta.setDisplayName(ChatColor.RED + achievement.name.replace('_', ' '));
			double progress = SqlApi.getStat(offlinePlayer, achievement.statType, server);
			try {
				String progressStr = StatType.valueOf(achievement.statType).valueToString(progress);
				String requiredStr = StatType.valueOf(achievement.statType).valueToString(achievement.required);
				lore.add("" + ChatColor.GRAY + ChatColor.ITALIC + progressStr + " / " + requiredStr);
			} catch (Exception e) {
				lore.add("" + ChatColor.GRAY + ChatColor.ITALIC + progress + " / " + achievement.required);
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
