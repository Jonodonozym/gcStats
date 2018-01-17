
package jdz.statsTracker.achievement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import jdz.bukkitUtils.sql.Database;
import jdz.statsTracker.GCStatsTracker;
import jdz.statsTracker.GCStatsTrackerConfig;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.stats.StatsDatabase;
import net.md_5.bungee.api.ChatColor;

public class AchievementDatabase extends Database implements Listener {
	private static final AchievementDatabase instance = new AchievementDatabase(GCStatsTracker.instance);

	public final String achievementPointsTable = "gcs_Achievement_Points";
	public final String achievementMetaTable = "gcs_Achievement_MetaData";
	private final String serverIconTable = "gcs_Server_MetaData";

	private final HashMap<Player, Integer> achievementPoints = new HashMap<Player, Integer>();

	public static AchievementDatabase getInstance() {
		return instance;
	}

	private AchievementDatabase(JavaPlugin plugin) {
		super(plugin);
		api.runOnConnect(() -> {
			ensureCorrectTables();
		});
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		onPlayerJoin(e.getPlayer());
	}

	public void onPlayerJoin(Player p) {
		String update = "INSERT INTO {table} (UUID) " + "SELECT '" + p.getName() + "' FROM dual "
				+ "WHERE NOT EXISTS ( SELECT UUID FROM {table} WHERE UUID = '" + p.getName() + "' ) LIMIT 1;";
		api.executeUpdateAsync(update.replaceAll("\\{table\\}", achievementPointsTable));
		for (String server : getServers())
			api.executeUpdateAsync(update.replaceAll("\\{table\\}", getAchTableName(server)));

		new BukkitRunnable() {
			@Override
			public void run() {
				for (StatType type : GCStatsTrackerConfig.enabledStats) {
					double value = StatsDatabase.getInstance().getStat(p, type);
					for (Achievement a : AchievementData.achievementsByType.get(GCStatsTrackerConfig.serverName)
							.get(type.toString()))
						if (a.isAchieved(value))
							setAchieved(p, a);
				}
			}
		}.runTaskAsynchronously(GCStatsTracker.instance);

		achievementPoints.put(p, getAchievementPoints(p));
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		onPlayerQuit(e.getPlayer());
	}

	public void onPlayerQuit(Player p) {
		achievementPoints.remove(p);
	}
	
	public boolean isConnected() {
		return api.isConnected();
	}

	public void awardAchievementPoints(Player p, int points) {
		if (!api.isConnected())
			return;
		String column = AchievementData.isGlobal ? "Global" : GCStatsTrackerConfig.serverName.replaceAll(" ", "_");
		String update = "UPDATE " + achievementPointsTable + " SET " + column + " = " + column + " + " + points
				+ " WHERE UUID = '" + p.getName() + "';";
		api.executeUpdateAsync(update);
	}

	public int getAchievementPoints(Player p) {
		try {
			return achievementPoints.get(p);
		} catch (NullPointerException e) {
			return (getAchievementPoints(p, GCStatsTrackerConfig.serverName.replaceAll(" ", "_")));
		}
	}

	public int getAchievementPoints(Player p, String server) {
		if (!api.isConnected())
			return 0;
		String query = "SELECT " + server + " FROM " + achievementPointsTable + " WHERE UUID = '" + p.getName() + "';";
		List<String[]> values = api.getRows(query);
		return (Integer.parseInt(values.get(0)[0]));
	}

	public List<Achievement> getAllAchievements() {
		List<Achievement> achievements = new ArrayList<Achievement>();
		if (!api.isConnected())
			return achievements;
		List<String> servers = StatsDatabase.getInstance().getServers();
		for (String server : servers)
			achievements.addAll(getServerAchievements(server));
		return achievements;
	}

	public List<Achievement> getServerAchievements(String server) {
		List<Achievement> achievements = new ArrayList<Achievement>();
		if (!api.isConnected())
			return achievements;
		String query = "SELECT * FROM " + achievementMetaTable + " WHERE server = '" + server.replaceAll(" ", "_")
				+ "';";
		List<String[]> result = api.getRows(query);
		for (String[] s : result) {
			String name = s[1];
			String statType = s[2];
			double required = Double.parseDouble(s[3]);
			int points = Integer.parseInt(s[4]);
			Material m = Material.valueOf(s[5]);
			short iconDamage = Short.parseShort(s[6]);
			String description = s[7];

			achievements.add(new Achievement(name, statType, required, points, m, iconDamage, description,
					server.replaceAll("_", " ")));
		}
		return achievements;
	}

	public boolean isAchieved(OfflinePlayer offlinePlayer, Achievement a) {
		if (!api.isConnected())
			return false;
		String query = "SELECT " + a.name.replace(' ', '_') + " FROM " + getAchTableName(a.server) + " WHERE UUID = '"
				+ offlinePlayer.getName() + "';";
		try {
			return Integer.parseInt(api.getRows(query).get(0)[0]) == 1;
		} catch (Exception e) {
			return true;
		}
	}

	public void setAchieved(Player p, Achievement a) {
		if (!api.isConnected())
			return;
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!isAchieved(p, a)) {
					String update = "UPDATE " + getAchTableName(a.server) + " SET " + a.name.replace(' ', '_')
							+ " = true WHERE UUID = '" + p.getName() + "';";
					api.executeUpdateAsync(update);
					if (AchievementData.awardPoints)
						awardAchievementPoints(p, a.points);
					a.doFirework(p);
				}
			}
		}.runTaskAsynchronously(GCStatsTracker.instance);
	}

	public List<String> getServers() {
		if (!api.isConnected())
			return new ArrayList<String>();
		List<String> columns = api.getColumns(achievementPointsTable);
		columns.remove("UUID");
		columns.remove("Global");
		List<String> servers = new ArrayList<String>();
		for (String s : columns)
			servers.add(s.replaceAll("_", " "));
		return servers;
	}

	public boolean hasServer(String server) {
		return (getServers().contains(server));
	}

	public void setServerIcon(String server, Material m, short damage) {
		if (!api.isConnected())
			return;
		String update = "REPLACE into " + serverIconTable + " (server, iconMaterial, iconDamage) values('"
				+ server.replaceAll(" ", "_") + "','" + m + "'," + damage + ");";
		api.executeUpdateAsync(update);
	}

	public ItemStack getServerIcon(String server) {
		if (!api.isConnected())
			return new ItemStack(Material.STONE);
		String query = "Select iconMaterial, iconDamage FROM " + serverIconTable + " WHERE server = '"
				+ server.replaceAll(" ", "_") + "';";
		List<String[]> list = api.getRows(query);
		Material m = Material.valueOf(list.get(0)[0]);
		short damage = Short.parseShort(list.get(0)[1]);
		ItemStack is = new ItemStack(m, 1, damage);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.GREEN + server.replaceAll("_", " "));
		is.setItemMeta(im);
		return is;
	}

	public void ensureCorrectTables() {
		ensureCorrectPointsTable();
		ensureCorrectServerIconTable();
	}

	private void ensureCorrectServerIconTable() {
		String update = "CREATE TABLE IF NOT EXISTS " + serverIconTable
				+ " (server varchar(127), iconMaterial varchar(63), iconDamage int);";
		api.executeUpdate(update);
	}

	private void ensureCorrectPointsTable() {
		String update = "CREATE TABLE IF NOT EXISTS " + achievementPointsTable + " (UUID varchar(127));";
		api.executeUpdate(update);

		List<String> columns = api.getColumns(achievementPointsTable);

		if (!columns.contains("Global"))
			api.executeUpdate("ALTER TABLE " + achievementPointsTable + " ADD COLUMN Global DOUBLE default 0");

		if (!columns.contains(GCStatsTrackerConfig.serverName.replaceAll(" ", "_")))
			api.executeUpdate("ALTER TABLE " + achievementPointsTable + " ADD COLUMN "
					+ GCStatsTrackerConfig.serverName.replaceAll(" ", "_") + " DOUBLE default 0");
	}

	void ensureCorrectAchMetaTable(HashMap<StatType, List<Achievement>> localAchievements) {
		if (!api.isConnected())
			return;
		String update = "CREATE TABLE IF NOT EXISTS " + achievementMetaTable
				+ "(server varchar(127), name varchar(127), statType varchar(63), required double, points int,"
				+ "icon varchar(63), iconDamage int, description varchar(1024));";
		api.executeUpdate(update);

		update = "DELETE FROM " + achievementMetaTable + " WHERE server = '"
				+ GCStatsTrackerConfig.serverName.replaceAll(" ", "_") + "';";
		api.executeUpdate(update);

		for (List<Achievement> list : localAchievements.values())
			for (Achievement a : list) {
				update = "INSERT INTO " + achievementMetaTable
						+ " (server,name,statType,required,points,icon,iconDamage,description) VALUES" + "('"
						+ a.server.replaceAll(" ", "_") + "','" + a.name.replace(' ', '_') + "','" + a.statType + "',"
						+ a.required + "," + a.points + ",'" + a.icon + "'," + a.iconDamage + ",'" + a.description
						+ "');";
				api.executeUpdate(update);
			}
	}

	void ensureCorrectAchTable(HashMap<StatType, List<Achievement>> localAchievements) {
		String update = "CREATE TABLE IF NOT EXISTS " + getAchTableName() + " (UUID varchar(127));";
		String columnsAddBoolean = "ALTER TABLE " + getAchTableName()
				+ " ADD COLUMN {column} Boolean NOT NULL default 0";
		api.executeUpdate(update);

		Set<String> columns = new HashSet<String>();
		columns.addAll(api.getColumns(getAchTableName()));

		for (List<Achievement> list : localAchievements.values())
			for (Achievement a : list)
				if (!columns.contains(a.name.replace(' ', '_')))
					api.executeUpdate(columnsAddBoolean.replaceAll("\\{column\\}", a.name.replace(' ', '_')));
	}

	private String getAchTableName() {
		return getAchTableName(GCStatsTrackerConfig.serverName);
	}

	private String getAchTableName(String server) {
		return "gcs_achievemnts_" + server.replaceAll(" ", "_");
	}
}
