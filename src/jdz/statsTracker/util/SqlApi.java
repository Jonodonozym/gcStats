package jdz.statsTracker.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import jdz.statsTracker.achievement.Achievement;
import jdz.statsTracker.main.Config;
import jdz.statsTracker.main.Main;
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

	private static String dbURL = "";
	private static String dbName = "";
	private static String dbUsername = "";
	private static String dbPassword = "";
	private static int dbReconnectTime = 1200;
	
	private static ConnectHook connectHook = null;
	
	public interface ConnectHook{
		public void run();
	}
	
	public static void addConnectHook(ConnectHook hook){
		connectHook = hook;
	}
	
	public static boolean reloadConfig(FileConfiguration config){
		dbURL = config.getString("database.URL");
		dbName = config.getString("database.name");
		dbUsername = config.getString("database.username");
		dbPassword = config.getString("database.password");
		dbReconnectTime = config.getInt("database.autoReconnectSeconds")*20;
		dbReconnectTime = dbReconnectTime<=0?1200:dbReconnectTime;
		
		if (dbURL.equals("") || dbName.equals("") || dbUsername.equals("") || dbPassword.equals("")) {
			Main.plugin.getLogger().info(
					"Some of the database lines in config.yml are empty, please fill in the config.yml and reload the plugin.");
			return false;
		} 
		return true;
	}
	
	private static Connection dbConnection = null;
	
	private static TimedTask autoReconnectTask = null;
	
	/**
	 * Opens a new connection to a specified SQL database If it fails 3 times,
	 * writes the error to a log file in the plugin's directory
	 * 
	 * @param logger
	 *            the logger to record success / fail messages to
	 * @return the opened connection, or null if one couldn't be created
	 */
	public static Connection open(Logger logger) {
		if (dbConnection != null)
			close(dbConnection);
		try {
			try {
				Class.forName(driver).newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				if (logger != null) ErrorLogger.createLog(e);
			}

			String url = "jdbc:mysql://" + dbURL + ":3306/" + dbName + "?user=" + dbUsername + "&password="
					+ dbPassword + "&loginTimeout=1000&useSSL=false";

			Connection dbConnection = DriverManager.getConnection(url, dbUsername, dbPassword);
			dbConnection.setNetworkTimeout(Executors.newFixedThreadPool(2), 15000);
			if (logger != null)
				logger.info("Successfully connected to the " + dbName + " database at the host " + dbURL);

			SqlApi.dbConnection = dbConnection;
			ensureCorrectTables();
			
			if (connectHook != null)
				connectHook.run();
			connectHook = null;
			
			return dbConnection;
		}

		catch (SQLException e) {
			if (logger != null){
				logger.info("Failed to connect to the database. Refer to the error log file in the plugin's directory"
						+ " and contact the database host / plugin developer to help resolve the issue.");
				ErrorLogger.createLog(e);
			}
			autoReconnect();
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
	
	public static boolean isConnected(){
		try {
			if (dbConnection != null && !dbConnection.isClosed())
				return true;
		} catch (SQLException e) { }
		return false;
	}
	
	private static boolean autoReconnect(){
		if (isConnected())
			return false;
		
		if (autoReconnectTask == null){
			autoReconnectTask = new TimedTask(dbReconnectTime, ()->{
				Connection con = SqlApi.open(null);
				if (con !=null)
				{
					Main.plugin.getLogger().info("Successfully re-connected to the database");
					dbConnection = con;
					if(autoReconnectTask != null)
						autoReconnectTask.stop();
					autoReconnectTask = null;
				}
			});
			autoReconnectTask.start();
		}
		return true;
	}

	
	public static void setServerMeta(String server, Material m, short damage){
		if (autoReconnect())
			return;
		String update = "REPLACE into "+serverMetaTable+" (server, iconMaterial, iconDamage) values('"+server.replaceAll(" ", "_")+"','"+m+"',"+damage+");";
		executeUpdate(update);
	}
	
	public static ItemStack getServerIcon(String server){
		if (autoReconnect())
			return new ItemStack(Material.STONE);
		String query = "Select iconMaterial, iconDamage FROM "+serverMetaTable+" WHERE server = '"+server.replaceAll(" ", "_")+"';";
		List<String[]> list = fetchRows(query);
		Material m =  Material.valueOf(list.get(0)[0]);
		short damage = Short.parseShort(list.get(0)[1]);
		ItemStack is = new ItemStack(m, 1, damage);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.GREEN+server.replaceAll("_", " "));
		is.setItemMeta(im);
		return is;
	}
	

	public static void addPlayer(Player p) {
		if (autoReconnect())
			return;
		String update = "INSERT INTO {table} (UUID) "+
			    "SELECT '"+p.getName()+"' FROM dual "+
			    "WHERE NOT EXISTS ( SELECT UUID FROM {table} WHERE UUID = '"+p.getName()+"' ) LIMIT 1;";
		executeUpdate(update.replaceAll("\\{table\\}",achievementPointsTable));
		for (String server: getServers()){
			executeUpdate(update.replaceAll("\\{table\\}",getStatTableName(server)));
			executeUpdate(update.replaceAll("\\{table\\}",getAchTableName(server)));
		}
	}
	
	public static boolean hasPlayer(String server, OfflinePlayer offlinePlayer){
		if (offlinePlayer == null)
			return false;
		if (autoReconnect())
			return false;
		String query = "SELECT * FROM " + getStatTableName(server.replaceAll(" ", "_")) + " WHERE UUID = '"+offlinePlayer.getName()+"';";
		List<String[]> result= fetchRows( query);
		return (!result.isEmpty());
	}

	public static void awardAchievementPoints(Player p, int points) {
		if (autoReconnect())
			return;
		String update = "UPDATE " + achievementPointsTable + " SET " + Config.serverName.replaceAll(" ", "_") + " = " + Config.serverName.replaceAll(" ", "_")
				+ " + " + points + " WHERE UUID = '" + p.getName() + "';";
		executeUpdate( update);
	}

	public static int getAchievementPoints(Player p) {
		return (getAchievementPoints(p, Config.serverName.replaceAll(" ", "_")));
	}

	public static int getAchievementPoints(Player p, String server) {
		if (autoReconnect())
			return 0;
		String query = "SELECT " + server + " FROM " + achievementPointsTable + " WHERE UUID = '" + p.getName() + "';";
		List<String[]> values = fetchRows( query);
		return (Integer.parseInt(values.get(0)[0]));
	}

	public static List<Achievement> getAllAchievements() {
		List<Achievement> achievements = new ArrayList<Achievement>();
		if (autoReconnect())
			return achievements;
		List<String> servers = getServers();
		for(String server: servers)
			achievements.addAll(getServerAchievements(server));
		return achievements;
	}

	public static List<Achievement> getServerAchievements(String server) {
		List<Achievement> achievements = new ArrayList<Achievement>();
		if (autoReconnect())
			return achievements;
		String query = "SELECT * FROM "+achievementMetaTable+" WHERE server = '"+server.replaceAll(" ", "_")+"';";
		List<String[]> result = fetchRows( query);
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

	public static boolean isAchieved(OfflinePlayer offlinePlayer, Achievement a) {
		if (autoReconnect())
			return false;
		String query = "SELECT "+a.name.replace(' ', '_')+" FROM "+getAchTableName(a.server)+" WHERE UUID = '"+offlinePlayer.getName()+"';";
		return Integer.parseInt(fetchRows( query).get(0)[0]) == 1;
	}

	public static void setAchieved(Player p, Achievement a) {
		if (autoReconnect())
			return;
		if(!isAchieved(p, a)){
			String update = "UPDATE "+getAchTableName(a.server)+" SET "+a.name.replace(' ', '_')+" = true WHERE UUID = '" + p.getName() +"';";
			executeUpdate( update);
			awardAchievementPoints(p, a.points);
			a.doFirework(p);
		}
	}

	public static double getStat(Player p, String statType) {
		return getStat(p, statType, Config.serverName.replaceAll(" ", "_"));
	}

	public static double getStat(OfflinePlayer offlinePlayer, String statType, String server) {
		if (autoReconnect())
			return 0;
		String query = "SELECT " + statType + " FROM " + getStatTableName(server) + " WHERE UUID = '" + offlinePlayer.getName() + "';";
		List<String[]> values = fetchRows( query);
		return Double.parseDouble(values.get(0)[0]);
	}

	public static void setStat(Player p, StatType stat, double newValue) {
		if (autoReconnect())
			return;
		String update = "UPDATE " + getStatTableName() + " SET " + stat + " = " + newValue + " WHERE UUID = '"
				+ p.getName() + "';";
		executeUpdate( update);
	}

	public static void addStat(Player p, StatType stat, double change) {
		if (autoReconnect())
			return;
		String update = "UPDATE " + getStatTableName() + " SET " + stat + " = " + stat + " + " + change
				+ " WHERE UUID = '" + p.getName() + "';";
		executeUpdate( update);
	}

	public static List<String> getServers() {
		if (autoReconnect())
			return new ArrayList<String>();
		List<String> columns = fetchColumns(achievementPointsTable);
		columns.remove("UUID");
		List<String> servers = new ArrayList<String>();
		for (String s: columns)
			servers.add(s.replaceAll("_", " "));
		return servers;
	}
	
	public static boolean hasServer(String server){
		if (autoReconnect())
			return false;
		return (getServers().contains(server));
	}
	
	public static void ensureCorrectTables(){
		ensureCorrectPointsTable();
		ensureCorrectStatMetaTable();
		ensureCorrectServerMetaTable();
		ensureCorrectStatTable();
	}
	
	private static void ensureCorrectPointsTable() {
		String update = "CREATE TABLE IF NOT EXISTS " + achievementPointsTable + " (UUID varchar(127));";
		executeUpdate( update);

		List<String> columns = fetchColumns(achievementPointsTable);
		if (!columns.contains(Config.serverName.replaceAll(" ", "_")))
			executeUpdate(
					"ALTER TABLE " + achievementPointsTable + " ADD COLUMN " + Config.serverName.replaceAll(" ", "_") + " DOUBLE default 0");
	}

	public static void ensureCorrectAchMetaTable(HashMap<StatType, List<Achievement>> localAchievements) {
		if (autoReconnect())
			return;
		String update = "CREATE TABLE IF NOT EXISTS " + achievementMetaTable 
				+ "(server varchar(127), name varchar(127), statType varchar(63), required double, points int,"
				+ "icon varchar(63), iconDamage int, description varchar(1024));";
		executeUpdate( update);
		
		update = "DELETE FROM "+achievementMetaTable+" WHERE server = '"+Config.serverName.replaceAll(" ", "_")+"';";
		executeUpdate( update);
		
		for(List<Achievement> list: localAchievements.values())
			for(Achievement a: list){
				update = "INSERT INTO "+achievementMetaTable+
						" (server,name,statType,required,points,icon,iconDamage,description) VALUES"+
						"('"+a.server.replaceAll(" ", "_")+"','"+a.name.replace(' ', '_')+"','"+a.statType+"',"+a.required+","+a.points+",'"+
						a.icon+"',"+a.iconDamage+",'"+a.description+"');";
				executeUpdate( update);
			}
	}
	
	private static void ensureCorrectStatMetaTable(){
		String newTable = "CREATE TABLE IF NOT EXISTS "+statsMetaTable+" (server varchar(127));";
		String newRow = "INSERT INTO "+statsMetaTable+" (server) "+
			    "SELECT '"+Config.serverName.replaceAll(" ", "_")+"' FROM dual "+
			    "WHERE NOT EXISTS ( SELECT server FROM "+statsMetaTable+" WHERE server = '"+Config.serverName.replaceAll(" ", "_")+"' ) LIMIT 1;";
		executeUpdate( newTable);
		executeUpdate( newRow);

		String columnsAddBoolean = "ALTER TABLE " + statsMetaTable + " ADD COLUMN {column} Boolean NOT NULL default 0";
		String setValue = "UPDATE "+statsMetaTable+" SET {column} = {value} WHERE server = '"+Config.serverName.replaceAll(" ", "_")+"';";
		Set<String> columns = new HashSet<String>();
		columns.addAll(fetchColumns(statsMetaTable));
		for(StatType s: StatType.values()){
			if (!columns.contains(s.toString()))
				executeUpdate( columnsAddBoolean.replaceAll("\\{column\\}", s.toString()));
			executeUpdate( setValue.replaceAll("\\{column\\}", s.toString()).replaceAll("\\{value\\}", Config.enabledStats.contains(s)+""));
		}
	}
	
	private static void ensureCorrectServerMetaTable(){
		String update = "CREATE TABLE IF NOT EXISTS "+serverMetaTable+" (server varchar(127), iconMaterial varchar(63), iconDamage int);";
		executeUpdate( update);
	}

	public static void ensureCorrectAchTable(HashMap<StatType, List<Achievement>> localAchievements) {
		String update = "CREATE TABLE IF NOT EXISTS " + getAchTableName() + " (UUID varchar(127));";
		String columnsAddBoolean = "ALTER TABLE " + getAchTableName() + " ADD COLUMN {column} Boolean NOT NULL default 0";
		executeUpdate( update);

		Set<String> columns = new HashSet<String>();
		columns.addAll(fetchColumns(getAchTableName()));
		
		for(List<Achievement> list: localAchievements.values())
			for(Achievement a: list)
				if (!columns.contains(a.name.replace(' ', '_')))
					executeUpdate( columnsAddBoolean.replaceAll("\\{column\\}", a.name.replace(' ', '_')));
	}

	private static void ensureCorrectStatTable() {
		String update = "CREATE TABLE IF NOT EXISTS " + getStatTableName() + " (UUID varchar(127));";
		executeUpdate( update);

		String columnsAdd = "ALTER TABLE " + getStatTableName() + " ADD COLUMN {column} DOUBLE DEFAULT 0";

		List<String> columns = fetchColumns(getStatTableName());

		for (StatType s : StatType.values())
			if (Config.enabledStats.contains(s))
				if (!columns.contains(s.toString()))
					executeUpdate( columnsAdd.replaceAll("\\{column\\}", s.toString()));
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
	private static List<String[]> fetchRows(String query) {
		List<String[]> rows = new ArrayList<String[]>();
		Statement stmt = null;
		try {
			stmt = dbConnection.createStatement();
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

	public static List<String> fetchColumns(String table) {
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
	public static boolean hasTable(String Table) {
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
	private static void executeUpdate(String update) {
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

	public static List<String> getEnabledStats(String server) {
		List<String> enabledStats = new ArrayList<String>();
		if (autoReconnect())
			return enabledStats;
		List<String> columns = fetchColumns(statsMetaTable);
		
		String query = "SELECT * FROM "+statsMetaTable+" WHERE server = '"+server.replaceAll(" ", "_")+"';";
		String[] row = fetchRows( query).get(0);
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
