
package jdz.statsTracker.main;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.util.SqlApi;

public class Config {
	public static String serverName = "";
	
	public static String dbURL = "";
	public static String dbName = "";
	public static String dbUsername = "";
	public static String dbPassword = "";
	
	public static Connection dbConnection = null;
	
	public static Map<StatType,Boolean> statEnabled = new HashMap<StatType,Boolean>();
	
	public static int autoUpdateDelay = 6000;
	public static int afkTime = 12000;

	public static String statsCommand = "gcs";
	public static String achCommand = "gca";
	
	public static List<String> servers = new ArrayList<String>();

	public static void reloadConfig(){
		File file = new File(Main.plugin.getDataFolder() + File.separator + "config.yml");
		if (!file.exists())
			Main.plugin.saveDefaultConfig();
		
		Main.plugin.reloadConfig();
		FileConfiguration config = Main.plugin.getConfig();

		serverName = config.getString("server.name");
		
		for (StatType s: StatType.values()){
			if (config.contains("statsEnabled."+s))
				statEnabled.put(s,config.getBoolean("statsEnabled."+s));
			else
				statEnabled.put(s, false);
		}
		
		dbURL = config.getString("database.URL");
		dbName = config.getString("database.name");
		dbUsername = config.getString("database.username");
		dbPassword = config.getString("database.password");

		if (dbURL.equals("") || dbName.equals("") || dbUsername.equals("") || dbPassword.equals("")){
			Main.plugin.getLogger().info("Some of the database lines in config.yml are empty, please fill in the config.yml and reload the plugin.");
		}
		else{
			SqlApi.close(dbConnection);
			dbConnection = SqlApi.open(Main.plugin.getLogger(), dbURL, 3306, dbName, dbUsername, dbPassword);
			if (dbConnection != null){
				SqlApi.ensureCorrectPointsTable(dbConnection);
				SqlApi.ensureCorrectStatTable(dbConnection);
				servers = SqlApi.getServers(dbConnection);
			}
			else for(StatType s: StatType.values())
				statEnabled.put(s, false);
		}
	}
}
