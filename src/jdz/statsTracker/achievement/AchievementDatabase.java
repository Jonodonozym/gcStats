
package jdz.statsTracker.achievement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import jdz.bukkitUtils.fileIO.FileLogger;
import jdz.bukkitUtils.sql.Database;
import jdz.statsTracker.GCStatsTracker;
import jdz.statsTracker.GCStatsTrackerConfig;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

public class AchievementDatabase extends Database implements Listener {
	@Getter
	private static final AchievementDatabase instance = new AchievementDatabase(GCStatsTracker.instance);

	public final String achievementPointsTable = "gcs_Achievement_Points";
	public final String achievementMetaTable = "gcs_Achievement_MetaData";
	private final String serverIconTable = "gcs_Server_MetaData";

	private AchievementDatabase(JavaPlugin plugin) {
		super(plugin);
		api.runOnConnect(() -> {
			ensureCorrectTables();
		});
	}

	public boolean isConnected() {
		return api.isConnected();
	}

	public void runOnConnect(Runnable r) {
		api.runOnConnect(r);
	}

	void addPlayer(Player p) {
		String update = "INSERT INTO {table} (UUID) " + "SELECT '" + p.getName() + "' FROM dual "
				+ "WHERE NOT EXISTS ( SELECT UUID FROM {table} WHERE UUID = '" + p.getName() + "' ) LIMIT 1;";
		api.executeUpdateAsync(update.replaceAll("\\{table\\}", achievementPointsTable));
		api.executeUpdateAsync(update.replaceAll("\\{table\\}", getAchTableName()));
	}

	void setAchievementPoints(Player p, int points) {
		String column = GCStatsTrackerConfig.achievementPointsGlobal ? "Global"
				: GCStatsTrackerConfig.serverName.replaceAll(" ", "_");
		String update = "UPDATE " + achievementPointsTable + " SET " + column + " = " + points + " WHERE UUID = '"
				+ p.getName() + "';";
		api.executeUpdateAsync(update);
	}

	public void addAchievementPoints(Player p, int points) {
		String column = GCStatsTrackerConfig.achievementPointsGlobal ? "Global"
				: GCStatsTrackerConfig.serverName.replaceAll(" ", "_");
		String update = "UPDATE " + achievementPointsTable + " SET " + column + " = " + column +" + " +points + " WHERE UUID = '"
				+ p.getName() + "';";
		api.executeUpdateAsync(update);
	}

	public int getAchievementPoints(OfflinePlayer p) {
		return (getAchievementPoints(p, GCStatsTrackerConfig.serverName.replaceAll(" ", "_")));
	}

	public int getAchievementPoints(OfflinePlayer p, String server) {
		if (!api.isConnected())
			return 0;
		String query = "SELECT " + server + " FROM " + achievementPointsTable + " WHERE UUID = '" + p.getName() + "';";
		List<String[]> values = api.getRows(query);
		return (Integer.parseInt(values.get(0)[0]));
	}

	public void addAchievements(Achievement[] achievements) {
		ExecutorService es = Executors.newFixedThreadPool(achievements.length);
		for (Achievement a : achievements) {
			if (a == null)
				continue;
			String update = "REPLACE INTO " + achievementMetaTable
					+ " (server,name,statType,required,points,icon,iconDamage,description) VALUES" + "('"
					+ GCStatsTrackerConfig.serverName.replaceAll(" ", "_") + "','" + a.getName().replaceAll(" ", "_")
					+ "','"
					+ (a instanceof StatAchievement ? ((StatAchievement) a).getStatType().getNameUnderscores() : "null")
					+ "'," + (a instanceof StatAchievement ? ((StatAchievement) a).getRequired() : "0") + ","
					+ a.getPoints() + ",'" + a.getIcon() + "'," + a.getIconDamage() + ",'" + a.getDescription() + "');";

			es.execute(() -> {
				api.executeUpdate(update);
			});
		}

		Set<String> columns = new HashSet<String>();
		columns.addAll(api.getColumns(getAchTableName()));
		String columnsAddBoolean = "ALTER TABLE " + getAchTableName()
				+ " ADD COLUMN {column} Boolean NOT NULL default 0";

		for (Achievement a : achievements)
			if (a == null)
				continue;
			else if (!columns.contains(a.getName().replace(' ', '_')))
				es.execute(() -> {
					api.executeUpdate(columnsAddBoolean.replaceAll("\\{column\\}", a.getName().replace(' ', '_')));
				});

		es.shutdown();
		try {
			es.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			new FileLogger(GCStatsTracker.instance).createErrorLog(e);
		}
	}

	List<RemoteAchievement> getServerAchievements(String server) {
		List<RemoteAchievement> achievements = new ArrayList<RemoteAchievement>();
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

			if (statType.equalsIgnoreCase("null"))
				achievements.add(new RemoteAchievement(name, points, m, iconDamage, description, server));
			else
				achievements.add(new RemoteStatAchievement(name, points, m, iconDamage, description, server, statType,
						required));
		}
		return achievements;
	}

	boolean isAchieved(OfflinePlayer offlinePlayer, Achievement a) {
		return isAchieved(offlinePlayer, a, GCStatsTrackerConfig.serverName);
	}

	boolean isAchieved(OfflinePlayer offlinePlayer, Achievement a, String server) {
		if (!api.isConnected())
			return true;
		String query = "SELECT " + a.getName().replace(' ', '_') + " FROM " + getAchTableName(server)
				+ " WHERE UUID = '" + offlinePlayer.getName() + "';";
		try {
			return Integer.parseInt(api.getRows(query).get(0)[0]) == 1;
		} catch (Exception e) {
			return true;
		}
	}

	void setAchieved(Player p, Achievement a) {
		String update = "UPDATE " + getAchTableName() + " SET " + a.getName().replace(' ', '_')
				+ " = true WHERE UUID = '" + p.getName() + "';";
		api.executeUpdateAsync(update);
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
		String update = "REPLACE INTO " + serverIconTable + " (server, iconMaterial, iconDamage) values('"
				+ server.replaceAll(" ", "_") + "','" + m + "'," + damage + ");";
		api.executeUpdate(update);
	}

	ItemStack getServerIcon(String server) {
		if (!api.isConnected())
			return new ItemStack(Material.STONE);
		String query = "SELECT iconMaterial, iconDamage FROM " + serverIconTable + " WHERE server = '"
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
		ensureCorrectAchMetaTable();
		ensureCorrectAchTable();
	}

	private void ensureCorrectServerIconTable() {
		String update = "CREATE TABLE IF NOT EXISTS " + serverIconTable
				+ " (server varchar(127), iconMaterial varchar(63), iconDamage int, PRIMARY KEY(server));";
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

	private void ensureCorrectAchMetaTable() {
		String update = "CREATE TABLE IF NOT EXISTS " + achievementMetaTable
				+ "(server varchar(63), name varchar(127), statType varchar(63), required double, points int,"
				+ "icon varchar(63), iconDamage int, description varchar(1024), PRIMARY KEY (server, name));";
		api.executeUpdate(update);
	}

	private void ensureCorrectAchTable() {
		String update = "CREATE TABLE IF NOT EXISTS " + getAchTableName() + " (UUID varchar(127));";
		api.executeUpdate(update);
	}

	private String getAchTableName() {
		return getAchTableName(GCStatsTrackerConfig.serverName);
	}

	private String getAchTableName(String server) {
		return "gcs_achievemnts_" + server.replaceAll(" ", "_");
	}
}
