package jdz.statsTracker.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import jdz.bukkitUtils.sql.SqlApi;
import jdz.statsTracker.achievement.Achievement;
import jdz.statsTracker.achievement.AchievementData;
import jdz.statsTracker.config.Config;
import net.md_5.bungee.api.ChatColor;

/**
 * Utility class with static methods to interact with the sql database
 * 
 * @author Jonodonozym
 */
public class StatsDatabase {
	private static StatsDatabase instance;

	public static StatsDatabase getInstance() {
		return instance;
	}

	private SqlApi api = null;

	public static StatsDatabase init(JavaPlugin plugin) {
		instance = new StatsDatabase();
		instance.api = new SqlApi(plugin);
		instance.api.runOnConnect(() -> {
			instance.ensureCorrectTables();
		});
		return instance;
	}
	
	public SqlApi getApi() {
		return api;
	}

	private static final String achievementPointsTable = "gcs_Achievement_Points";
	private static final String achievementMetaTable = "gcs_Achievement_MetaData";
	private static final String serverMetaTable = "gcs_Server_MetaData";
	private static final String statsMetaTable = "gcs_Stat_MetaData";

	public void setServerMeta(String server, Material m, short damage) {
		String update = "REPLACE into " + serverMetaTable + " (server, iconMaterial, iconDamage) values('"
				+ server.replaceAll(" ", "_") + "','" + m + "'," + damage + ");";
		api.executeUpdateAsync(update);
	}

	public ItemStack getServerIcon(String server) {
		if (!api.isConnected())
			return new ItemStack(Material.STONE);
		String query = "Select iconMaterial, iconDamage FROM " + serverMetaTable + " WHERE server = '"
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

	public void addPlayer(Player p) {
			String update = "INSERT INTO {table} (UUID) " + "SELECT '" + p.getName() + "' FROM dual "
					+ "WHERE NOT EXISTS ( SELECT UUID FROM {table} WHERE UUID = '" + p.getName() + "' ) LIMIT 1;";
			api.executeUpdate(update.replaceAll("\\{table\\}", achievementPointsTable));
			for (String server : getServers()) {
				api.executeUpdateAsync(update.replaceAll("\\{table\\}", getStatTableName(server)));
				api.executeUpdateAsync(update.replaceAll("\\{table\\}", getAchTableName(server)));
			}
	}

	public boolean hasPlayer(String server, OfflinePlayer offlinePlayer) {
		if (offlinePlayer == null)
			return false;
		String query = "SELECT * FROM " + getStatTableName(server.replaceAll(" ", "_")) + " WHERE UUID = '"
				+ offlinePlayer.getName() + "';";
		List<String[]> result = api.getRows(query);
		return (!result.isEmpty());
	}

	public void awardAchievementPoints(Player p, int points) {
		String column = AchievementData.isGlobal?"Global":Config.serverName.replaceAll(" ", "_");
		String update = "UPDATE " + achievementPointsTable + " SET " + column + " = "
				+ column + " + " + points + " WHERE UUID = '" + p.getName() + "';";
		api.executeUpdateAsync(update);
	}

	public int getAchievementPoints(Player p) {
		return (getAchievementPoints(p, Config.serverName.replaceAll(" ", "_")));
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
		List<String> servers = getServers();
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
			return true;
		String query = "SELECT " + a.name.replace(' ', '_') + " FROM " + getAchTableName(a.server) + " WHERE UUID = '"
				+ offlinePlayer.getName() + "';";
		try {
		return Integer.parseInt(api.getRows(query).get(0)[0]) == 1;
		}
		catch (Exception e) {
			return true;
		}
	}

	public void setAchieved(Player p, Achievement a) {
		if (!isAchieved(p, a)) {
			String update = "UPDATE " + getAchTableName(a.server) + " SET " + a.name.replace(' ', '_')
					+ " = true WHERE UUID = '" + p.getName() + "';";
			api.executeUpdateAsync(update);
			if (AchievementData.awardPoints)
			awardAchievementPoints(p, a.points);
			a.doFirework(p);
		}
	}

	public double getStat(Player p, String statType) {
		return getStat(p, statType, Config.serverName.replaceAll(" ", "_"));
	}

	public double getStat(OfflinePlayer offlinePlayer, String statType, String server) {
		if (!api.isConnected())
			return 0;
		
		String query = "SELECT " + statType + " FROM " + getStatTableName(server) + " WHERE UUID = '"
				+ offlinePlayer.getName() + "';";
		List<String[]> values = api.getRows(query);
		
		try {
			return Double.parseDouble(values.get(0)[0]);
		}
		catch (Exception e) {
			return 0;
		}
	}

	public void setStat(Player p, StatType stat, double newValue) {
		if (StatBuffer.containsType(stat))
			StatBuffer.setStat(p, stat, newValue);
		else
			setStatDirect(p, stat, newValue);
	}

	public void addStat(Player p, StatType stat, double change) {
		if (StatBuffer.containsType(stat))
			StatBuffer.addStat(p, stat, change);
		else
			addStatDirect(p, stat, change);
	}

	public void setStatDirect(Player p, StatType stat, double newValue) {
		String update = "UPDATE " + getStatTableName() + " SET " + stat + " = " + newValue + " WHERE UUID = '"
				+ p.getName() + "';";
		api.executeUpdateAsync(update);
	}

	public void addStatDirect(Player p, StatType stat, double change) {
		String update = "UPDATE " + getStatTableName() + " SET " + stat + " = " + stat + " + " + change
				+ " WHERE UUID = '" + p.getName() + "';";
		api.executeUpdateAsync(update);
	}

	public List<String> getServers() {
		if (api.isConnected())
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
		if (!api.isConnected())
			return false;
		return (getServers().contains(server));
	}


	public List<String> getEnabledStats(String server) {
		List<String> enabledStats = new ArrayList<String>();
		if (!api.isConnected())
			return enabledStats;
		List<String> columns = api.getColumns(statsMetaTable);

		String query = "SELECT * FROM " + statsMetaTable + " WHERE server = '" + server.replaceAll(" ", "_") + "';";
		String[] row = api.getRows(query).get(0);
		int i = 0;
		for (String s : columns) {
			try {
				if (Integer.parseInt(row[i++]) == 1)
					enabledStats.add(s);
			} catch (NumberFormatException e) {
			}
		}

		return enabledStats;
	}
	
	

	private String getStatTableName() {
		return getStatTableName(Config.serverName);
	}

	private String getStatTableName(String server) {
		return "gcs_stats_" + server.replaceAll(" ", "_");
	}

	private String getAchTableName() {
		return getAchTableName(Config.serverName);
	}

	private String getAchTableName(String server) {
		return "gcs_achievemnts_" + server.replaceAll(" ", "_");
	}
	

	
	
	
	

	private void ensureCorrectTables() {
		ensureCorrectPointsTable();
		ensureCorrectStatMetaTable();
		ensureCorrectServerMetaTable();
		ensureCorrectStatTable();
	}

	private void ensureCorrectPointsTable() {
		String update = "CREATE TABLE IF NOT EXISTS " + achievementPointsTable + " (UUID varchar(127));";
		api.executeUpdate(update);

		List<String> columns = api.getColumns(achievementPointsTable);
		
		if (!columns.contains("Global"))
			api.executeUpdate("ALTER TABLE " + achievementPointsTable + " ADD COLUMN Global DOUBLE default 0");
		
		if (!columns.contains(Config.serverName.replaceAll(" ", "_")))
			api.executeUpdate("ALTER TABLE " + achievementPointsTable + " ADD COLUMN "
					+ Config.serverName.replaceAll(" ", "_") + " DOUBLE default 0");
	}	
		
	private void ensureCorrectStatMetaTable() {
		String newTable = "CREATE TABLE IF NOT EXISTS " + statsMetaTable + " (server varchar(127));";
		String newRow = "INSERT INTO " + statsMetaTable + " (server) " + "SELECT '"
				+ Config.serverName.replaceAll(" ", "_") + "' FROM dual " + "WHERE NOT EXISTS ( SELECT server FROM "
				+ statsMetaTable + " WHERE server = '" + Config.serverName.replaceAll(" ", "_") + "' ) LIMIT 1;";
		api.executeUpdate(newTable);
		api.executeUpdate(newRow);

		String columnsAddBoolean = "ALTER TABLE " + statsMetaTable + " ADD COLUMN {column} Boolean NOT NULL default 0";
		String setValue = "UPDATE " + statsMetaTable + " SET {column} = {value} WHERE server = '"
				+ Config.serverName.replaceAll(" ", "_") + "';";
		Set<String> columns = new HashSet<String>();
		columns.addAll(api.getColumns(statsMetaTable));
		for (StatType s : StatType.values()) {
			if (!columns.contains(s.toString()))
				api.executeUpdate(columnsAddBoolean.replaceAll("\\{column\\}", s.toString()));
			api.executeUpdate(setValue.replaceAll("\\{column\\}", s.toString()).replaceAll("\\{value\\}",
					Config.enabledStats.contains(s) + ""));
		}
	}
	
	private void ensureCorrectServerMetaTable() {
		String update = "CREATE TABLE IF NOT EXISTS " + serverMetaTable
				+ " (server varchar(127), iconMaterial varchar(63), iconDamage int);";
		api.executeUpdate(update);
	}
	
	private void ensureCorrectStatTable() {
		String update = "CREATE TABLE IF NOT EXISTS " + getStatTableName() + " (UUID varchar(127));";
		api.executeUpdate(update);

		String columnsAdd = "ALTER TABLE " + getStatTableName() + " ADD COLUMN {column} DOUBLE DEFAULT 0";

		List<String> columns = api.getColumns(getStatTableName());

		for (StatType s : StatType.values())
			if (Config.enabledStats.contains(s))
				if (!columns.contains(s.toString()))
					api.executeUpdate(columnsAdd.replaceAll("\\{column\\}", s.toString()));
	}

	public void ensureCorrectAchMetaTable(HashMap<StatType, List<Achievement>> localAchievements) {
		String update = "CREATE TABLE IF NOT EXISTS " + achievementMetaTable
				+ "(server varchar(127), name varchar(127), statType varchar(63), required double, points int,"
				+ "icon varchar(63), iconDamage int, description varchar(1024));";
		api.executeUpdate(update);

		update = "DELETE FROM " + achievementMetaTable + " WHERE server = '" + Config.serverName.replaceAll(" ", "_")
				+ "';";
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
	
	public void ensureCorrectAchTable(HashMap<StatType, List<Achievement>> localAchievements) {
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

}
