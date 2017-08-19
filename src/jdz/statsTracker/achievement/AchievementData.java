
package jdz.statsTracker.achievement;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import jdz.statsTracker.main.Config;
import jdz.statsTracker.main.Main;
import jdz.statsTracker.stats.StatType;
import jdz.statsTracker.util.FileExporter;
import jdz.statsTracker.util.SqlApi;

public class AchievementData {	
	public static Map<Player, Map<Achievement,Boolean>> serverAchievementData = new HashMap<Player, Map<Achievement,Boolean>>();
	public static Map<String,Map<StatType,List<Achievement>>> achievements = new HashMap<String, Map<StatType,List<Achievement>>>();
	
	public static void addPlayer(Player p){
		serverAchievementData.put(p, new HashMap<Achievement,Boolean>());
		for (String server: Config.servers){
			List<String> statTypes = SqlApi.fetchColumns(Config.dbConnection, server);
			statTypes.remove("UUID");
			for (String s: statTypes){
				StatType type = StatType.valueOf(s);
				double value = SqlApi.getStat(Config.dbConnection, p, type, server);
				for (Achievement a: achievements.get(server).get(type)){
					serverAchievementData.get(p).put(a, a.isAchieved(value));
				}
			}
		}
	}
	
	public static void removePlayer(Player p){
		serverAchievementData.remove(p);
	}
	
	public static void loadAchievements(){
		// loading from the local file
		String location = Main.plugin.getDataFolder().getPath()+File.separator+"Achievements.yml";
		File file = new File(location);
		if (!file.exists())
			FileExporter.ExportResource("Achievements.yml", location);
		
		HashMap<StatType,List<Achievement>> localAchievements = new HashMap<StatType,List<Achievement>>();
		FileConfiguration achConfig = YamlConfiguration.loadConfiguration(file);
			for(StatType type: StatType.values())
				if(Config.statEnabled.get(type))
					localAchievements.put(type, new ArrayList<Achievement>());
			for(String achievement: achConfig.getConfigurationSection("achievements").getKeys(false)){
				try{
					StatType type = StatType.valueOf(achConfig.getString("achievements."+achievement+"type"));
					if (!Config.statEnabled.get(type))
						continue;
					List<Integer> required = achConfig.getIntegerList("achievements."+achievement+"required");
					List<Integer> points = achConfig.getIntegerList("achievements."+achievement+"points");
					String description = achConfig.getString("achievements."+achievement+"description");
					Material m = Material.GRASS;;
					try{
						m = Material.valueOf(achConfig.getString("achievements."+achievement+"icon"));
					}
					catch (Exception e){ }
					for (int i=0; i<required.size(); i++){
						Achievement ach = new Achievement(achievement, type, required.get(i), points.get(i), m,
								description.replaceAll("\\{required\\}", required.get(i)+""), Config.serverName);
						localAchievements.get(type).add(ach);
					}
				}
				catch(Exception e){
					Main.plugin.getLogger().info("achievement "+achievement+" has invalid configuration, skipping...");
				}
			}
			
		// updating the sql db to match
		SqlApi.ensureCorrectAchMetaTable(Config.dbConnection, localAchievements);
		SqlApi.ensureCorrectAchTable(Config.dbConnection, localAchievements);
		
		// fetching the entire achievements db from the sql db
		for(String s: SqlApi.getServers(Config.dbConnection))
			achievements.put(s, new HashMap<StatType,List<Achievement>>());
		
		for(Achievement a: SqlApi.getAllAchievements(Config.dbConnection)){
			if (!achievements.get(a.server).containsKey(a.statType))
				achievements.get(a.server).put(a.statType, new ArrayList<Achievement>());
			achievements.get(a.server).get(a.statType).add(a);
		}
	}
	
	public static void updateAchievements(Player p, StatType s){
		double value = SqlApi.getStat(Config.dbConnection, p, StatType.BLOCKS_MINED);
		for(Achievement a: achievements.get(Config.serverName).get(s)){
			if(serverAchievementData.get(p).get(a))
				continue;
			if(a.isAchieved(value)){
				serverAchievementData.get(p).put(a, true);
				SqlApi.setAchieved(Config.dbConnection, p, a);
			}
		}
	}
}
