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
import org.bukkit.entity.Player;

import jdz.statsTracker.achievement.Achievement;
import jdz.statsTracker.main.Config;
import jdz.statsTracker.stats.StatType;

/**
 * Utility class with static methods to interact with the sql database
 * 
 * @author Jonodonozym
 */
public class SqlApi {
	public static final String driver = "com.mysql.jdbc.Driver";
	public static final String achievementPointsTable = "Achievement_Points";
	public static final String achievementMetaTable = "Achievement_MetaData";

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
					+ password + "&loginTimeout=1000";

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

	public static void addPlayer(Connection dbConnection, Player p) {
		String update = "BEGIN IF NOT EXISTS (SELECT * FROM " + getStatTableName() + ") WHERE UUID = '" + p.getName()
				+ "' BEGIN INSERT INTO " + getStatTableName() + " (UUID) VALUES ('" + p.getName() + "') END END";
		executeUpdate(dbConnection, update);
		
		update = "BEGIN IF NOT EXISTS (SELECT * FROM " + achievementPointsTable + ") WHERE UUID = '" + p.getName()
				+ "' BEGIN INSERT INTO " + achievementPointsTable + " (UUID) VALUES ('" + p.getName() + "') END END";
		executeUpdate(dbConnection, update);
		
		update = "BEGIN IF NOT EXISTS (SELECT * FROM " + getAchTableName() + ") WHERE UUID = '" + p.getName()
		+ "' BEGIN INSERT INTO " + getAchTableName() + " (UUID) VALUES ('" + p.getName() + "') END END";
		executeUpdate(dbConnection, update);
	}

	public static void awardAchievementPoints(Connection dbConnection, Player p, int points) {
		String update = "UPDATE " + achievementPointsTable + " SET " + getStatTableName() + " = " + getStatTableName()
				+ " + " + points + " WHERE UUID = '" + p.getName() + "';";
		executeUpdate(dbConnection, update);
	}

	public static int getAchievementPoints(Connection dbConnection, Player p) {
		return (getAchievementPoints(dbConnection, p, Config.serverName));
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
		String query = "SELECT * FROM "+achievementMetaTable+" WHERE server = '"+server+"';";
		List<String[]> result = fetchRows(dbConnection, query);
		List<Achievement> achievements = new ArrayList<Achievement>();
		for(String[] s: result){
			String name = s[1];
			StatType statType = StatType.valueOf(s[2]);
			double required = Double.parseDouble(s[3]);
			int points = Integer.parseInt(s[4]);
			Material m = Material.valueOf(s[5]);
			String description = s[6];
			
			achievements.add(new Achievement(name, statType, required, points, m, description, server));
		}
		return achievements;
	}

	public static boolean isAchieved(Connection dbConnection, Player p, Achievement a) {
		String query = "SELECT "+a.name+" FROM "+getAchTableName(a.server)+" WHERE UUID = '"+p.getName()+"';";
		return Boolean.parseBoolean(fetchRows(dbConnection, query).get(0)[0]);
	}

	public static void setAchieved(Connection dbConnection, Player p, Achievement a) {
		if(!isAchieved(dbConnection, p, a)){
			String update = "UPDATE "+getAchTableName(a.server)+" SET "+a.name+" = true WHERE UUID = '" + p.getName() +"';";
			executeUpdate(dbConnection, update);
			awardAchievementPoints(dbConnection, p, a.points);
		}
	}
	
	

	public static int getStat(Connection dbConnection, Player p, StatType stat) {
		return getStat(dbConnection, p, stat, Config.serverName);
	}

	public static int getStat(Connection dbConnection, Player p, StatType stat, String server) {
		String query = "SELECT " + stat + " FROM " + getStatTableName(server) + " WHERE UUID = '" + p.getName() + "';";
		List<String[]> values = fetchRows(dbConnection, query);
		return Integer.parseInt(values.get(0)[0]);
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
		List<String> servers = fetchColumns(dbConnection, achievementPointsTable);
		servers.remove("UUID");
		return servers;
	}

	public static void ensureCorrectPointsTable(Connection dbConnection) {
		String update = "CREATE TABLE IF NOT EXISTS '" + achievementPointsTable + "' (UUID varchar(127));";
		executeUpdate(dbConnection, update);

		List<String> columns = fetchColumns(dbConnection, achievementPointsTable);
		if (!columns.contains(Config.serverName))
			executeUpdate(dbConnection,
					"ALTER TABLE " + achievementPointsTable + " ADD COLUMN " + Config.serverName + " DOUBLE NOT NULL");
	}

	public static void ensureCorrectAchMetaTable(Connection dbConnection, HashMap<StatType, List<Achievement>> localAchievements) {
		String update = "CREATE TABLE IF NOT EXISTS '" + achievementMetaTable + "'"
				+ "(server varchar(127), name varchar(127), statType varchar(63), required double, points int,"
				+ "icon varchar(63), description varchar(1024));";
		executeUpdate(dbConnection, update);
		
		update = "DELETE FROM "+achievementMetaTable+" WHERE server = '"+Config.serverName+"';";
		executeUpdate(dbConnection, update);
		
		for(List<Achievement> list: localAchievements.values())
			for(Achievement a: list){
				update = "INSERT INTO "+achievementMetaTable+
						" (server,name,statType,required,points,icon,description) VALUES"+
						"('"+a.server+"','"+a.name+"','"+a.statType+"',"+a.required+","+a.points+",'"+
						a.icon+"','"+a.description+"',);";
				executeUpdate(dbConnection, update);
			}
	}

	public static void ensureCorrectAchTable(Connection dbConnection, HashMap<StatType, List<Achievement>> localAchievements) {
		String update = "CREATE TABLE IF NOT EXISTS '" + getAchTableName() + "' (UUID varchar(127));";
		String columnsAddBoolean = "ALTER TABLE " + getStatTableName() + " ADD COLUMN {column} Boolean NOT NULL default 0";
		executeUpdate(dbConnection, update);

		Set<String> columns = new HashSet<String>(fetchColumns(dbConnection, achievementPointsTable));
		for(List<Achievement> list: localAchievements.values())
			for(Achievement a: list)
				if (!columns.contains(a.name))
					executeUpdate(dbConnection, columnsAddBoolean.replaceAll("\\{column\\}", a.name));
	}

	public static void ensureCorrectStatTable(Connection dbConnection) {
		String update = "CREATE TABLE IF NOT EXISTS '" + getStatTableName() + "' (UUID varchar(127));";
		executeUpdate(dbConnection, update);

		String columnsAdd = "ALTER TABLE " + getStatTableName() + " ADD COLUMN {column} DOUBLE DEFAULT 0";
		String columnsDrop = "ALTER TABLE DROP " + getStatTableName() + " COLUMN {column};";

		List<String> columns = fetchColumns(dbConnection, getStatTableName());

		for (StatType s : StatType.values()) {
			if (Config.statEnabled.get(s)) {
				if (!columns.contains(s.toString()))
					executeUpdate(dbConnection, columnsAdd.replaceAll("\\{column\\}", s.toString()));
			} else if (columns.contains(s.toString()))
				executeUpdate(dbConnection, columnsDrop.replaceAll("\\{column\\}", s.toString()));

		}
	}

	private static String getStatTableName() {
		return getStatTableName(Config.serverName);
	}

	private static String getStatTableName(String server) {
		return "StatsTracker_" + server;
	}

	private static String getAchTableName() {
		return getAchTableName(Config.serverName);
	}

	private static String getAchTableName(String server) {
		return "AchievementTracker_" + server;
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
			while (rs.next()) {
				int size = rs.getFetchSize();
				String[] row = new String[size];
				for (int i = 0; i < size; i++)
					row[i] = rs.getString(i);
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
		String query = "SHOW columns FROM " + getStatTableName() + ";";
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
	public static boolean hasTable(Connection connection, String Table) {
		boolean returnValue = false;
		String query = "SHOW TABLES LIKE '" + Table + "';";
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
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
	private static void executeUpdate(Connection connection, String update) {
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
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
}
