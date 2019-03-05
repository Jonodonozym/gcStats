package jdz.statsTracker.achievement;

import java.util.ArrayList;
import java.util.Comparator;
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

import jdz.bukkitUtils.fileIO.FileLogger;
import jdz.bukkitUtils.utils.ItemUtils;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.achievement.achievementTypes.RemoteStatAchievement;
import jdz.statsTracker.achievement.achievementTypes.StatAchievement;
import jdz.statsTracker.achievement.database.AchievementDatabase;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsDatabase;
import lombok.Getter;
import lombok.Setter;

public class AchievementInventories implements Listener {
	@Getter private static final AchievementInventories instance = new AchievementInventories();

	protected AchievementInventories() {}

	protected final String SERVER_SELECT_INV_NAME = ChatColor.DARK_GREEN + "Achievements: server select";
	protected Inventory serverSelect;

	protected Map<String, List<Achievement>> allAchievements = new HashMap<>();
	protected Map<Achievement, ItemStack> achievementToStack = new HashMap<>();

	protected Map<Player, OfflinePlayer> targets = new HashMap<>();
	protected Map<Player, Integer> page = new HashMap<>();
	protected Map<Player, String> server = new HashMap<>();

	private final Achievement emptyAchievement = new Achievement("NULL", Material.AIR, (short) 0, "") {};

	public void reload() {
		targets.clear();

		List<String> servers = AchievementDatabase.getInstance().getServers();

		if (servers.size() > 0) {
			ExecutorService es = Executors.newFixedThreadPool(servers.size());
			for (String server : servers) {
				if (server.equals(GCStatsConfig.serverName))
					continue;
				es.execute(() -> {
					List<Achievement> remoteAchievements = AchievementDatabase.getInstance()
							.getServerAchievements(server);
					remoteAchievements.sort((a, b) -> {
						return a.getName().compareTo(b.getName());
					});

					addEmptySlots(remoteAchievements);
					allAchievements.put(server, remoteAchievements);
				});
			}

			es.shutdown();
			try {
				es.awaitTermination(1, TimeUnit.MINUTES);
			}
			catch (InterruptedException e) {
				new FileLogger(GCStats.getInstance()).createErrorLog(e);
			}
		}

		updateLocalAchievements();
		achievementToStack = createDefaultItemStacks();
		achievementToStack.put(emptyAchievement, new ItemStack(Material.AIR));
		serverSelect = createServerSelectInv();
	}

	private void addEmptySlots(List<Achievement> achievements) {
		for (int index = 0; index < achievements.size(); index++) {
			Achievement a = achievements.get(index);
			if (a.isNewLineBefore())
				for (int i = index % 9; i < 9; i++)
					achievements.add(index, emptyAchievement);
			if (a.isNewLineAfter())
				for (int i = index % 9 + 1; i < 9; i++)
					achievements.add(index + 1, emptyAchievement);
		}
	}

	public void updateLocalAchievements() {
		List<Achievement> achievements = new ArrayList<>(AchievementManager.getInstance().getAchievements());
		if (sortMode != null)
			achievements.sort(sortMode);
		for (Achievement a : achievements) {
			ItemStack itemStack = new ItemStack(a.getIcon(), a.getIconQuantity());
			ItemUtils.setData(itemStack, a.getIconDamage());
			achievementToStack.put(a, itemStack);
		}
		addEmptySlots(achievements);
		allAchievements.put(GCStatsConfig.serverName, achievements);
	}

	@Setter private Comparator<Achievement> sortMode = null;

	public void openServerAchievements(Player p, OfflinePlayer target) {
		openServerAchievements(p, target, GCStatsConfig.serverName);
	}

	public void openServerAchievements(Player p, OfflinePlayer target, String server) {
		targets.put(p, target);
		openPage(p, server, 0);
	}

	public void openServerSelect(Player player, OfflinePlayer target) {
		targets.put(player, target);
		player.openInventory(serverSelect);
	}

	private void openPage(Player p, String server, int number) {
		page.put(p, number);
		this.server.put(p, GCStatsConfig.serverName);
		p.openInventory(getPageInventory(p, targets.get(p), server, number));
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		Inventory inv = e.getInventory();

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
	private Inventory createServerSelectInv() {
		List<ItemStack> itemStacks = new ArrayList<>();

		for (String server : AchievementDatabase.getInstance().getServers())
			itemStacks.add(AchievementDatabase.getInstance().getServerIcon(server));

		int rows = (int) Math.ceil(itemStacks.size() / 4.0);
		Inventory inv = Bukkit.createInventory(null, rows * 9, SERVER_SELECT_INV_NAME);

		int index = 0;
		for (ItemStack itemStack : itemStacks) {
			int location = index * 2 + 1 + index / 4;
			if (rows - 1 <= index / 4)
				switch (itemStacks.size() % 4) {
				case 1:
					location = (rows - 1) * 9 + 4;
					break;
				case 2:
					location = (rows - 1) * 9 + 3 + index % 4 * 2;
					break;
				case 3:
					location = (rows - 1) * 9 + 2 + index % 4 * 2;
					break;
				}
			inv.setItem(location, itemStack);
			index++;
		}

		return inv;
	}

	private Map<Achievement, ItemStack> createDefaultItemStacks() {
		Map<Achievement, ItemStack> achToItem = new HashMap<>();
		for (List<? extends Achievement> list : allAchievements.values())
			for (Achievement a : list) {
				ItemStack itemStack = new ItemStack(a.getIcon());
				ItemUtils.setData(itemStack, a.getIconDamage());
				achToItem.put(a, itemStack);
			}
		return achToItem;
	}

	private Inventory getPageInventory(Player user, OfflinePlayer offlinePlayer, String server, int page) {
		List<? extends Achievement> achievements = allAchievements.get(server);

		if (sortMode != null) {
			achievements = new ArrayList<>(allAchievements.get(server));
			achievements.sort(sortMode);
		}

		Inventory pageInventory = Bukkit.createInventory(null, 54,
				ChatColor.DARK_GREEN + offlinePlayer.getName() + ChatColor.DARK_GREEN + "'s Achievements");

		int i = 0;
		for (int achIndex = page * 45; achIndex < Math.min((page + 1) * 45, achievements.size()); achIndex++) {
			Achievement achievement = achievements.get(achIndex);
			final int f = i++;
			if (!achievement.equals(emptyAchievement))
				Bukkit.getScheduler().runTaskAsynchronously(GCStats.getInstance(), () -> {
					ItemStack itemStack = getPlayerStack(user, offlinePlayer, achievement);
					pageInventory.setItem(f, itemStack);
				});
		}

		if ((page + 1) * 45 < achievements.size())
			pageInventory.setItem(53, new ChangePageArrow(ArrowType.NEXT));
		if (page > 0)
			pageInventory.setItem(45, new ChangePageArrow(ArrowType.PREVIOUS));
		pageInventory.setItem(49, new ChangePageArrow(ArrowType.SELECT_SERVER));

		return pageInventory;
	}

	private ItemStack getPlayerStack(Player user, OfflinePlayer offlinePlayer, Achievement achievement) {
		boolean isAchieved = AchievementManager.getInstance().isAchieved(offlinePlayer, achievement);
		ItemStack newStack = new ItemStack(achievementToStack.get(achievement));
		ItemMeta itemMeta = newStack.getItemMeta();

		List<String> lore = new ArrayList<>();

		if (!achievement.isHidden() || isAchieved && user.equals(offlinePlayer.getPlayer()))
			for (String s : achievement.getDescription())
				lore.add(ChatColor.YELLOW + s);
		else
			for (String s : achievement.getDescription())
				lore.add(ChatColor.GRAY + s.replaceAll("[^\\s]", "?"));

		for (Achievement requirement : achievement.getPreRequisites())
			if (!requirement.isAchieved(offlinePlayer))
				lore.add(ChatColor.RED + "Requires: " + requirement.getName());

		if (isAchieved) {
			itemMeta.addEnchant(Enchantment.DURABILITY, 10, true);
			lore.add(ChatColor.GREEN + "Achievement Unlocked!");
		}

		if (isAchieved && (!achievement.isHidden() || user.equals(offlinePlayer.getPlayer()))) {
			if (!achievement.getRewardText()[0].equals(""))
				lore.add(0, ChatColor.GREEN + "Reward: " + ChatColor.WHITE + ChatColor.ITALIC
						+ achievement.getRewardText()[0]);
			for (int i = 1; i < achievement.getRewardText().length; i++)
				lore.add(i, ChatColor.WHITE + "" + ChatColor.ITALIC + achievement.getRewardText()[i]);
			lore.add(achievement.getRewardText().length, "");

			itemMeta.setDisplayName(ChatColor.GREEN + achievement.getName().replace('_', ' '));
		}
		else {
			itemMeta.setDisplayName(ChatColor.RED + achievement.getName().replace('_', ' '));
			if (achievement.isHidden()) {
				if (!achievement.getRewardText()[0].equals(""))
					lore.add(ChatColor.GREEN + "Reward: " + ChatColor.GRAY + ChatColor.ITALIC
							+ achievement.getRewardText()[0].replaceAll("[^\\s]", "?"));
				for (int i = 1; i < achievement.getRewardText().length; i++)
					lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC
							+ achievement.getRewardText()[i].replaceAll("[^\\s]", "?"));

				newStack.setType(Material.BEDROCK);
			}
			else {
				if (!achievement.getRewardText()[0].equals(""))
					lore.add(ChatColor.GREEN + "Reward: " + ChatColor.GRAY + ChatColor.ITALIC
							+ achievement.getRewardText()[0]);
				for (int i = 1; i < achievement.getRewardText().length; i++)
					lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + achievement.getRewardText()[i]);
			}
			if (achievement instanceof RemoteStatAchievement || achievement instanceof StatAchievement) {
				lore.add("");

				double progress;
				double required;
				StatType type;

				if (achievement instanceof RemoteStatAchievement) {
					RemoteStatAchievement rsa = (RemoteStatAchievement) achievement;
					String server = rsa.getServer().replaceAll("_", " ");
					required = rsa.getRequirement();
					type = rsa.getStatType();
					if (server.equalsIgnoreCase(GCStatsConfig.serverName) && offlinePlayer.isOnline())
						progress = type.get(offlinePlayer.getPlayer());
					else
						progress = StatsDatabase.getInstance().getStat(offlinePlayer, rsa.getStatTypeName(), server);
				}
				else {
					StatAchievement sa = (StatAchievement) achievement;
					type = sa.getStatType();
					progress = type.get(offlinePlayer);
					required = sa.getRequired();
				}

				if (type == null)
					lore.add("" + ChatColor.GRAY + ChatColor.ITALIC + progress + " / " + required);
				else {
					String progressStr = type.valueToString(progress);
					String requiredStr = type.valueToString(required);
					lore.add("" + ChatColor.GRAY + ChatColor.ITALIC + progressStr + " / " + requiredStr);
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
			setType(type == ArrowType.SELECT_SERVER ? Material.IRON_DOOR : Material.ARROW);
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
