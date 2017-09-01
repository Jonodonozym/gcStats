package jdz.statsTracker.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import jdz.statsTracker.achievement.Achievement;
import jdz.statsTracker.main.Config;
import jdz.statsTracker.stats.StatType;
import net.md_5.bungee.api.ChatColor;

/**
 * Utility class with static methods to interact with the sql database
 * 
 * @author Jonodonozym
 */
public class SqlApi {
	public static final String driver = "com.mysql.jdbc.Driver";
	public static final String achievementPointsTable = "gcs_Achievement_Points";
	public static final String achievementMetaTable = "gcs_Achievement_MetaData";
	public static final String serverMetaTable = "gcs_Server_MetaData";
	public static final String statsMetaTable = "gcs_Stat_MetaData";

	/**
	 * Opens a new connection to a specified SQL database If it fails 3 times,
	 * writes the error to a log file in the plugin's directory
	 * 
	 * @param logger
	 *            the logger to record success / fail messages to
	 * @return the opened connection, or null if one couldn't be created
	 */
	public static Connection open(Logger logger, String host, int port, String databaseName, String username,
			String password) {
		try {
			try {
				Class.forName(driver).newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				ErrorLogger.createLog(e);
			}

			String url = "jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?user=" + username + "&password="
					+ password + "&loginTimeout=1000&useSSL=false";

			Connection connection = DriverManager.getConnection(url, username, password);
			connection.setNetworkTimeout(Executors.newFixedThreadPool(2), 15000);
			logger.info("Successfully connected to the " + databaseName + " database at the host " + host);

			return connection;
		}

		catch (SQLException e) {
			logger.info("Failed to connect to the database. Refer to the error log file in the plugin's directory"
					+ " and contact the database host / plugin developer to help resolve the issue.");
			ErrorLogger.createLog(e);
		}
		return null;
	}

	/**
	 * Closes a given connection, catching any errors
	 * 
	 * @param connection
	 */
	public static boolean close(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
				return true;
			} catch (SQLException e) {
			}
		}
		return false;
	}
	

	
	public static void setServerMeta(Connection dbConnection, String server, Material m, short damage){
		String update = "REPLACE into "+serverMetaTable+" (server, iconMaterial, iconDamage) values('"+server.replaceAll(" ", "_")+"','"+m+"',"+damage+");";
		executeUpdate(dbConnection, update);
	}
	
	public static ItemStack getServerIcon(Connection dbConnection, String server){
		String query = "Select iconMaterial, iconDamage FROM "+serverMetaTable+" WHERE server = '"+server.replaceAll(" ", "_")+"';";
		List<String[]> list = fetchRows(dbConnection, query);
		Material m =  Material.valueOf(list.get(0)[0]);
		short damage = Short.parseShort(list.get(0)[1]);
		ItemStack is = new ItemStack(m, 1, damage);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.GREEN+server.replaceAll("_", " "));
		is.setItemMeta(im);
		return is;
	}
	

	public static void addPlayer(Connection dbConnection, Player p) {
		String update = "INSERT INTO {table} (UUID) "+
			    "SELECT '"+p.getName()+"' FROM dual "+
			    "WHERE NOT EXISTS ( SELECT UUID FROM {table} WHERE UUID = '"+p.getName()+"' ) LIMIT 1;";
		executeUpdate(dbConnection, update.replaceAll("\\{table\\}",achievementPointsTable));
		for (String server: getServers(dbConnection)){
			executeUpdate(dbConnection, update.replaceAll("\\{table\\}",getStatTableName(server)));
			executeUpdate(dbConnection, update.replaceAll("\\{table\\}",getAchTableName(server)));
		}
	}
	
	public static boolean hasPlayer(Connection dbConnection, String server, OfflinePlayer offlinePlayer){
		if (offlinePlayer == null)
			return false;
		String query = "SELECT * FROM " + getStatTableName(server.replaceAll(" ", "_")) + " WHERE UUID = '"+offlinePlayer.getName()+"';";
		List<String[]> result= fetchRows(dbConnection, query);
		return (!result.isEmpty());
	}

	public static void awardAchievementPoints(Connection dbConnection, Player p, int points) {
		String update = "UPDATE " + achievementPointsTable + " SET " + Config.serverName.replaceAll(" ", "_") + " = " + Config.serverName.replaceAll(" ", "_")
				+ " + " + points + " WHERE UUID = '" + p.getName() + "';";
		executeUpdate(dbConnection, update);
	}

	public static int getAchievementPoints(Connection dbConnection, Player p) {
		return (getAchievementPoints(dbConnection, p, Config.serverName.replaceAll(" ", "_")));
	}

	public static int getAchievementPoints(Connection dbConnection, Player p, String server) {
		String query = "SELECT " + server + " FROM " + achievementPointsTable + " WHERE UUID = '" + p.getName() + "';";
		List<String[]> values = fetchRows(dbConnection, query);
		return (Integer.parseInt(values.get(0)[0]));
	}

	public static List<Achievement> getAllAchievements(Connection dbConnection) {
		List<Achievement> achievements = new ArrayList<Achievement>();
		List<String> servers = getServers(dbConnection);
		for(String server: servers)
			achievements.addAll(getServerAchievements(dbConnection, server));
		return achievements;
	}

	public static List<Achievement> getServerAchievements(Connection dbConnection, String server) {
		String query = "SELECT * FROM "+achievementMetaTable+" WHERE server = '"+server.replaceAll(" ", "_")+"';";
		List<String[]> result = fetchRows(dbConnection, query);
		List<Achievement> achievements = new ArrayList<Achievement>();
		for(String[] s: result){
			String name = s[1];
			String statType = s[2];
			double required = Double.parseDouble(s[3]);
			int points = Integer.parseInt(s[4]);
			Material m = Material.valueOf(s[5]);
			short iconDamage = Short.parseShort(s[6]);
			String description = s[7];
			
			achievements.add(new Achievement(name, statType, required, points, m, iconDamage, description, server.replaceAll("_", " ")));
		}
		return achievements;
	}

	public static boolean isAchieved(Connection dbConnection, OfflinePlayer offlinePlayer, Achievement a) {
		String query = "SELECT "+a.name.replace(' ', '_')+" FROM "+getAchTableName(a.server)+" WHERE UUID = '"+offlinePlayer.getName()+"';";
		return Integer.parseInt(fetchRows(dbConnection, query).get(0)[0]) == 1;
	}

	public static void setAchieved(Connection dbConnection, Player p, Achievement a) {
		if(!isAchieved(dbConnection, p, a)){
			String update = "UPDATE "+getAchTableName(a.server)+" SET "+a.name.replace(' ', '_')+" = true WHERE UUID = '" + p.getName() +"';";
			executeUpdate(dbConnection, update);
			awardAchievementPoints(dbConnection, p, a.points);
			a.doFirework(p);
		}
	}

	public static double getStat(Connection dbConnection, Player p, String statType) {
		return getStat(dbConnection, p, statType, Config.serverName.replaceAll(" ", "_"));
	}

	public static double getStat(Connection dbConnection, OfflinePlayer offlinePlayer, String statType, String server) {
		String query = "SELECT " + statType + " FROM " + getStatTableName(server) + " WHERE UUID = '" + offlinePlayer.getName() + "';";
		List<String[]> values = fetchRows(dbConnection, query);
		return Double.parseDouble(values.get(0)[0]);
	}

	public static void setStat(Connection dbConnection, Player p, StatType stat, double newValue) {
		String update = "UPDATE " + getStatTableName() + " SET " + stat + " = " + newValue + " WHERE UUID = '"
				+ p.getName() + "';";
		executeUpdate(dbConnection, update);
	}

	public static void addStat(Connection dbConnection, Player p, StatType stat, double change) {
		String update = "UPDATE " + getStatTableName() + " SET " + stat + " = " + stat + " + " + change
				+ " WHERE UUID = '" + p.getName() + "';";
		executeUpdate(dbConnection, update);
	}

	public static List<String> getServers(Connection dbConnection) {
		List<String> columns = fetchColumns(dbConnection, achievementPointsTable);
		columns.remove("UUID");
		List<String> servers = new ArrayList<String>();
		for (String s: columns)
			servers.add(s.replaceAll("_", " "));
		return servers;
	}
	
	public static boolean hasServer(Connection dbConnection, String server){
		return (getServers(dbConnection).contains(server));
	}
	
	public static void ensureCorrectPointsTable(Connection dbConnection) {
		String update = "CREATE TABLE IF NOT EXISTS " + achievementPointsTable + " (UUID varchar(127));";
		executeUpdate(dbConnection, update);

		List<String> columns = fetchColumns(dbConnection, achievementPointsTable);
		if (!columns.contains(Config.serverName.replaceAll(" ", "_")))
			executeUpdate(dbConnection,
					"ALTER TABLE " + achievementPointsTable + " ADD COLUMN " + Config.serverName.replaceAll(" ", "_") + " DOUBLE default 0");
	}

	public static void ensureCorrectAchMetaTable(Connection dbConnection, HashMap<StatType, List<Achievement>> localAchievements) {
		String update = "CREATE TABLE IF NOT EXISTS " + achievementMetaTable 
				+ "(server varchar(127), name varchar(127), statType varchar(63), required double, points int,"
				+ "icon varchar(63), iconDamage int, description varchar(1024));";
		executeUpdate(dbConnection, update);
		
		update = "DELETE FROM "+achievementMetaTable+" WHERE server = '"+Config.serverName.replaceAll(" ", "_")+"';";
		executeUpdate(dbConnection, update);
		
		for(List<Achievement> list: localAchievements.values())
			for(Achievement a: list){
				update = "INSERT INTO "+achievementMetaTable+
						" (server,name,statType,required,points,icon,iconDamage,description) VALUES"+
						"('"+a.server.replaceAll(" ", "_")+"','"+a.name.replace(' ', '_')+"','"+a.statType+"',"+a.required+","+a.points+",'"+
						a.icon+"',"+a.iconDamage+",'"+a.description+"');";
				executeUpdate(dbConnection, update);
			}
	}
	
	public static void ensureCorrectStatMetaTable(Connection dbConnection){
		String newTable = "CREATE TABLE IF NOT EXISTS "+statsMetaTable+" (server varchar(127));";
		String newRow = "INSERT INTO "+statsMetaTable+" (server) "+
			    "SELECT '"+Config.serverName.replaceAll(" ", "_")+"' FROM dual "+
			    "WHERE NOT EXISTS ( SELECT server FROM "+statsMetaTable+" WHERE server = '"+Config.serverName.replaceAll(" ", "_")+"' ) LIMIT 1;";
		executeUpdate(dbConnection, newTable);
		executeUpdate(dbConnection, newRow);

		String columnsAddBoolean = "ALTER TABLE " + statsMetaTable + " ADD COLUMN {column} Boolean NOT NULL default 0";
		String setValue = "UPDATE "+statsMetaTable+" SET {column} = {value} WHERE server = '"+Config.serverName.replaceAll(" ", "_")+"';";
		Set<String> columns = new HashSet<String>();
		columns.addAll(fetchColumns(dbConnection, statsMetaTable));
		for(StatType s: StatType.values()){
			if (!columns.contains(s.toString()))
				executeUpdate(dbConnection, columnsAddBoolean.replaceAll("\\{column\\}", s.toString()));
			executeUpdate(dbConnection, setValue.replaceAll("\\{column\\}", s.toString()).replaceAll("\\{value\\}", Config.enabledStats.contains(s)+""));
		}
	}
	
	public static void ensureCorrectServerMetaTable(Connection dbConnection){
		String update = "CREATE TABLE IF NOT EXISTS "+serverMetaTable+" (server varchar(127), iconMaterial varchar(63), iconDamage int);";
		executeUpdate(dbConnection, update);
	}

	public static void ensureCorrectAchTable(Connection dbConnection, HashMap<StatType, List<Achievement>> localAchievements) {
		String update = "CREATE TABLE IF NOT EXISTS " + getAchTableName() + " (UUID varchar(127));";
		String columnsAddBoolean = "ALTER TABLE " + getAchTableName() + " ADD COLUMN {column} Boolean NOT NULL default 0";
		executeUpdate(dbConnection, update);

		Set<String> columns = new HashSet<String>();
		columns.addAll(fetchColumns(dbConnection, getAchTableName()));
		
		for(List<Achievement> list: localAchievements.values())
			for(Achievement a: list)
				if (!columns.contains(a.name.replace(' ', '_')))
					executeUpdate(dbConnection, columnsAddBoolean.replaceAll("\\{column\\}", a.name.replace(' ', '_')));
	}

	public static void ensureCorrectStatTable(Connection dbConnection) {
		String update = "CREATE TABLE IF NOT EXISTS " + getStatTableName() + " (UUID varchar(127));";
		executeUpdate(dbConnection, update);

		String columnsAdd = "ALTER TABLE " + getStatTableName() + " ADD COLUMN {column} DOUBLE DEFAULT 0";

		List<String> columns = fetchColumns(dbConnection, getStatTableName());

		for (StatType s : StatType.values())
			if (Config.enabledStats.contains(s))
				if (!columns.contains(s.toString()))
					executeUpdate(dbConnection, columnsAdd.replaceAll("\\{column\\}", s.toString()));
	}

	private static String getStatTableName() {
		return getStatTableName(Config.serverName);
	}

	private static String getStatTableName(String server) {
		return "gcs_stats_" + server.replaceAll(" ", "_");
	}

	private static String getAchTableName() {
		return getAchTableName(Config.serverName);
	}

	private static String getAchTableName(String server) {
		return "gcs_achievemnts_" + server.replaceAll(" ", "_");
	}

	/**
	 * Executes a query, returning the rows if the database responds with them
	 * 
	 * @param connection
	 * @param query
	 * @return
	 */
	private static List<String[]> fetchRows(Connection connection, String query) {
		List<String[]> rows = new ArrayList<String[]>();
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			int columns = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				String[] row = new String[columns];
				for (int i = 1; i <= columns; i++)
					row[i-1] = rs.getString(i);
				if (row.length > 0)
					rows.add(row);
			}
		} catch (SQLException e) {
			ErrorLogger.createLog(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					ErrorLogger.createLog(e);
				}
			}
		}
		return rows;
	}

	public static List<String> fetchColumns(Connection dbConnection, String table) {
		List<String> columns = new ArrayList<String>();
		String query = "SHOW columns FROM " + table + ";";
		Statement stmt = null;
		try {
			stmt = dbConnection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next())
				columns.add(rs.getString("Field"));
		} catch (SQLException e) {
			ErrorLogger.createLog(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					ErrorLogger.createLog(e);
				}
			}
		}
		return columns;
	}

	/**
	 * Checks to see if the database has a table
	 * 
	 * @param connection
	 * @param Table
	 * @return
	 */
	public static boolean hasTable(Connection dbConnection, String Table) {
		boolean returnValue = false;
		String query = "SHOW TABLES LIKE '" + Table + "';";
		Statement stmt = null;
		try {
			stmt = dbConnection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				returnValue = true;
			}
		} catch (SQLException e) {
			ErrorLogger.createLog(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					ErrorLogger.createLog(e);
				}
			}
		}
		return returnValue;
	}

	/**
	 * Executes a database update
	 * 
	 * @param connection
	 * @param update
	 */
	private static void executeUpdate(Connection dbConnection, String update) {
		Statement stmt = null;
		try {
			stmt = dbConnection.createStatement();
			stmt.executeUpdate(update);
		} catch (SQLException e) {
			ErrorLogger.createLog(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					ErrorLogger.createLog(e);
				}
			}
		}
	}

	public static List<String> getEnabledStats(Connection dbConnection, String server) {
		List<String> enabledStats = new ArrayList<String>();
		List<String> columns = fetchColumns(dbConnection, statsMetaTable);
		
		String query = "SELECT * FROM "+statsMetaTable+" WHERE server = '"+server.replaceAll(" ", "_")+"';";
		String[] row = fetchRows(dbConnection, query).get(0);
		int i=0;
		for (String s: columns){
			try{
				if (Integer.parseInt(row[i++]) == 1)
					enabledStats.add(s);
			}
			catch (NumberFormatException e){}
		}

		return enabledStats;
	}
}
