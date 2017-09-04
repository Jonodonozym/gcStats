
package jdz.statsTracker.achievement;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import jdz.statsTracker.main.Main;
import jdz.statsTracker.util.FileExporter;
import jdz.statsTracker.util.SqlApi;

public class AchievementShop implements Listener{
	private static boolean isEnabled = false;
	private static Inventory inventory;
	private static Map<Integer, ShopItem> items;
	
	public static void reload(){
		String location = Main.plugin.getDataFolder().getPath() + File.separator + "AchievementShop.yml";
		File file = new File(location);
		if (!file.exists())
			FileExporter.ExportResource("/AchievementShop.yml", location);

		FileConfiguration shopConfig = YamlConfiguration.loadConfiguration(file);
		
		items = new HashMap<Integer, ShopItem>();
		for (String s: shopConfig.getConfigurationSection("shop").getKeys(false)){
			if (s.equals("enabled"))
				isEnabled = shopConfig.getBoolean("shop.enabled");
			else{
				try{
					ShopItem item = ShopItem.fromConfig(shopConfig, "shop."+s);
					items.put(item.slot, item);
				}
				catch (Exception e){
					Main.plugin.getLogger().info(s+" in achievements shop has invalid configuration, ("+e.getMessage()+") skipping...");
					e.printStackTrace();
				}
			}	
		}
		
		int max = 0;
		for (Integer i: items.keySet())
			if (i > max)
				max = i;
		int rows = (int)Math.ceil((max+1)/9.0)*9;
		
		inventory = Bukkit.createInventory(null, rows, ChatColor.DARK_GREEN+"Achievement shop");
		for (Integer i: items.keySet())
			inventory.setItem(i, items.get(i).displayItem);
	}
	
	public static void openShop(Player p){
		if (isEnabled)
			p.openInventory(inventory);
		else
			p.sendMessage(ChatColor.RED+"The achievements shop is currently disabled for this server.");
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		Player p = (Player)e.getWhoClicked();
		Inventory inv = p.getOpenInventory().getTopInventory();
        
		if (inv != null && inv.getName().equals(ChatColor.DARK_GREEN+"Achievement shop")){
			int slot = e.getSlot();
			if (items.containsKey(slot)){
				ShopItem item = items.get(slot);
				int currentPoints = SqlApi.getAchievementPoints(p);
				if (SqlApi.getAchievementPoints(p) >= item.cost ){
					giveItems(item.items, p);
					for (String s: item.commands)
						Main.plugin.getServer().dispatchCommand(Main.plugin.getServer().getConsoleSender(), 
								s.replaceAll("\\{player\\}", p.getName()));
					if (item.messages.length > 0)
						p.sendMessage(item.messages);
					p.closeInventory();
					SqlApi.awardAchievementPoints(p, -item.cost);
					Main.plugin.getServer().dispatchCommand(p, "gca bal");
				}
				else
					p.sendMessage(ChatColor.RED+"You need "+(item.cost-currentPoints)+" more points for that");
			}
			e.setCancelled(true);
		}
	}
	
	private static void giveItems(Collection<ItemStack> items, Player p){	
		boolean wasExtra = false;
		for (ItemStack item: items){
			ItemStack copy = new ItemStack(item);
			
			String purchasedBy = ChatColor.GREEN+"Purchased by: "+ChatColor.WHITE+p.getDisplayName();
			ItemMeta im = copy.getItemMeta();
			List<String> lore = new ArrayList<String>();
			if (copy.getItemMeta().getLore() != null)
				lore = copy.getItemMeta().getLore();
			lore.add(0,purchasedBy);
			im.setLore(lore);
			copy.setItemMeta(im);
			
			HashMap<Integer, ItemStack> excess = p.getInventory().addItem(copy);
			for (ItemStack drop : excess.values()){
				p.getWorld().dropItem(p.getLocation(), drop);
				wasExtra = true;
			}
		}
		if (wasExtra) p.sendMessage(ChatColor.GRAY+"Inventory was full, placed extra items on floor");
	}
	
	private static class ShopItem{
		public final ItemStack displayItem;
		public final List<ItemStack> items;
		public final int slot, cost;
		public final List<String> commands;
		public final String[] messages;
		
		private ShopItem(ItemStack displayItem, List<ItemStack> items, int slot, int cost, List<String> commands, List<String> messages, boolean giveItem){
			this.items = items;
			if (giveItem)
				this.items.add(0, new ItemStack(displayItem));
			
			this.displayItem = displayItem;
			String priceTag = ChatColor.DARK_AQUA+"Price: "+ChatColor.GOLD+cost+ChatColor.DARK_AQUA+" points.";
			ItemMeta im = displayItem.getItemMeta();
			List<String> lore = new ArrayList<String>();
			if (displayItem.getItemMeta().getLore() != null)
				lore = displayItem.getItemMeta().getLore();
			lore.add(0,priceTag);
			im.setLore(lore);
			this.displayItem.setItemMeta(im);
			
			this.slot = slot;
			this.cost = cost;
			this.commands = commands;
			this.messages = messages.toArray(new String[messages.size()]);
		}
		
		@SuppressWarnings("deprecation")
		public static ItemStack stackFromConfig(FileConfiguration shopConfig, String key){
			String customName = "";
			int id = shopConfig.getInt(key+".ItemID");
			int amount = 1;
			int enchantCode = -1;
			int enchantLevel = 1;
			int data = 0;
			List<String> lore = new ArrayList<String>();

			if (shopConfig.contains(key+".ItemAmount"))
				amount = shopConfig.getInt(key+".ItemAmount");
			if (shopConfig.contains(key+".EnchantCode"))
				enchantCode = shopConfig.getInt(key+".EnchantCode");
			if (shopConfig.contains(key+".EnchantLevel"))
				enchantLevel = shopConfig.getInt(key+".EnchantLevel");
			if (shopConfig.contains(key+".ItemData"))
				data = shopConfig.getInt(key+".ItemData");
			if (shopConfig.contains(key+".CustomName"))
				customName = shopConfig.getString(key+".CustomName").replaceAll("&", "§");
			if (shopConfig.contains(key+".ItemLore"))
				lore = shopConfig.getStringList(key+".ItemLore");
			
			ItemStack item = new ItemStack(Material.getMaterial(id), amount, (short)data);
			ItemMeta im = item.getItemMeta();
			if (customName != "")
				im.setDisplayName(customName);
			im.setLore(lore);
			item.setItemMeta(im);
			if (enchantCode != -1)
				item.addUnsafeEnchantment(Enchantment.getById(enchantCode), enchantLevel);
			
			return item;
		}

		public static ShopItem fromConfig(FileConfiguration shopConfig, String key){
			if (!shopConfig.contains(key+".Cost"))
				throw new RuntimeException("Cost missing from config");
			
			ItemStack displayItem = stackFromConfig(shopConfig, key);
			int slot = Integer.parseInt(key.substring(9));
			int cost = shopConfig.getInt(key+".Cost");
			boolean giveItem = shopConfig.getBoolean(key+".GiveItem");
			
			List<String> commands = new ArrayList<String>();
			List<String> messages = new ArrayList<String>();
			
			if (shopConfig.contains(key+".EnchantCode"))
				commands = shopConfig.getStringList(key+".EnchantCode");
			if (shopConfig.contains(key+".PlayerMessages"))
				messages = shopConfig.getStringList(key+".PlayerMessages");

			List<ItemStack> extraItems = new ArrayList<ItemStack>();
			if (shopConfig.contains(key+".ExtraItems")){
				for (String itemKey: shopConfig.getConfigurationSection(key+".ExtraItems").getKeys(false)){
					extraItems.add(stackFromConfig(shopConfig, key+".ExtraItems."+itemKey));
				}
			}
			
			return new ShopItem(displayItem, extraItems, slot, cost, commands, messages, giveItem);
		}
	}
}
