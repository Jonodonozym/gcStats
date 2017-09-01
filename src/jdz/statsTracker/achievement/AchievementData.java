
package jdz.statsTracker.achievement;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
import jdz.statsTracker.util.RomanNumber;
import jdz.statsTracker.util.SqlApi;

public class AchievementData {
	public static Map<String, Map<String, List<Achievement>>> achievementsByType = new HashMap<String, Map<String, List<Achievement>>>();
	public static Map<String, List<Achievement>> achievements = new HashMap<String, List<Achievement>>();
	public static Map<Achievement, Integer> numTiers = new HashMap<Achievement, Integer>();
	
	public static void addPlayer(Player p) {
		for (String server : Config.servers) {
			List<String> statTypes = SqlApi.getEnabledStats(Config.dbConnection, server);
			statTypes.remove("UUID");
			for (String type : statTypes) {
				double value = SqlApi.getStat(Config.dbConnection, p, type.toString(), server);
				for (Achievement a : achievementsByType.get(server).get(type))
					if (a.isAchieved(value))
						SqlApi.setAchieved(Config.dbConnection, p, a);
			}
		}
	}

	public static void reloadData() {
		// loading from the local file
		String location = Main.plugin.getDataFolder().getPath() + File.separator + "Achievements.yml";
		File file = new File(location);
		if (!file.exists())
			FileExporter.ExportResource("/Achievements.yml", location);

		HashMap<StatType, List<Achievement>> localAchievements = new HashMap<StatType, List<Achievement>>();
		FileConfiguration achConfig = YamlConfiguration.loadConfiguration(file);
		for (StatType type : Config.enabledStats)
			localAchievements.put(type, new ArrayList<Achievement>());
		for (String achievement : achConfig.getConfigurationSection("achievements").getKeys(false)) {
			try {
				StatType type = StatType.valueOf(achConfig.getString("achievements." + achievement + ".type"));
				if (!Config.enabledStats.contains(type)){
					Main.plugin.getLogger().info("achievement " + achievement + " stat type is disabled in config.yml, skipping...");
					continue;
				}
				String description = achConfig.getString("achievements." + achievement + ".description");
				Material m = Material.GRASS;
				try {
					m = Material.valueOf(achConfig.getString("achievements." + achievement + ".icon"));
				} catch (Exception e) {
				}
				short iconDamage = (short)achConfig.getInt("achievements." + achievement + ".iconDamage");

				List<Double> required = achConfig.getDoubleList("achievements." + achievement + ".required");
				List<Integer> points = achConfig.getIntegerList("achievements." + achievement + ".points");
				if (required == null) {
					required = new ArrayList<Double>();
					points = new ArrayList<Integer>();
					required.add(achConfig.getDouble("achievements." + achievement + ".required"));
					points.add(achConfig.getInt("achievements." + achievement + ".points"));
				}

				for (int i = 0; i < required.size(); i++) {
					String name = achievement + (required.size() == 1 ? "" : " " + RomanNumber.toRoman(i+1));
					Achievement ach = new Achievement(name, type.toString(), required.get(i), points.get(i), m, iconDamage,
							description.replaceAll("\\{required\\}", type.valueToString(required.get(i)) + ""), Config.serverName);
					localAchievements.get(type).add(ach);
					numTiers.put(ach, required.size());
				}

			} catch (Exception e) {
				Main.plugin.getLogger().info("achievement " + achievement + " has invalid configuration, skipping...");
			}
		}

		// updating the sql db to match
		SqlApi.ensureCorrectAchMetaTable(Config.dbConnection, localAchievements);
		SqlApi.ensureCorrectAchTable(Config.dbConnection, localAchievements);
		
		achievements.clear();

		// fetching the entire achievements db from the sql db
		for (String s : Config.servers) {
			achievementsByType.put(s, new HashMap<String, List<Achievement>>());
			achievements.put(s, new ArrayList<Achievement>());
			for (String stat : SqlApi.getEnabledStats(Config.dbConnection, s))
				achievementsByType.get(s).put(stat, new ArrayList<Achievement>());
		}

		for (Achievement a : SqlApi.getAllAchievements(Config.dbConnection)) {
			achievementsByType.get(a.server).get(a.statType).add(a);
			achievements.get(a.server).add(a);
		}
		
		for (String s: Config.servers) {
			Collections.sort(achievements.get(s),  (a,b)->{
				return ((Achievement)a).name.compareTo(((Achievement)b).name);
			});
		}
	}

	public static void updateAchievements(Player p, StatType s) {
		double value = SqlApi.getStat(Config.dbConnection, p, s.toString());
		for (Achievement a : achievementsByType.get(Config.serverName).get(s.toString()))
			if (a.isAchieved(value))
				SqlApi.setAchieved(Config.dbConnection, p, a);
	}
}
