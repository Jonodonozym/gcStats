
package jdz.statsTracker.achievement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import jdz.bukkitUtils.fileIO.FileLogger;
import jdz.bukkitUtils.misc.StringUtils;
import jdz.bukkitUtils.sql.Database;
import jdz.bukkitUtils.sql.SqlColumn;
import jdz.bukkitUtils.sql.SqlColumnType;
import jdz.statsTracker.GCStatsTracker;
import jdz.statsTracker.GCStatsTrackerConfig;
import static jdz.bukkitUtils.sql.SqlColumnType.*;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

public class AchievementDatabase extends Database implements Listener {
	@Getter private static final AchievementDatabase instance = new AchievementDatabase(GCStatsTracker.instance);

	private final String achievementPointsTable = "gcs_Achievement_Points";
	private final SqlColumn[] achievementPointsTablColumns = new SqlColumn[] {
			new SqlColumn("UUID", SqlColumnType.STRING_64, true), new SqlColumn("Global", SqlColumnType.INT) };
	private final String achievementMetaTable = "gcs_Achievement_MetaData";
	private final SqlColumn[] achievementMetaColumns = new SqlColumn[] { new SqlColumn("server", STRING_64, true),
			new SqlColumn("name", STRING_128, true), new SqlColumn("statType", STRING_64),
			new SqlColumn("required", DOUBLE), new SqlColumn("points", INT), new SqlColumn("icon", STRING_32),
			new SqlColumn("iconDamage", INT), new SqlColumn("description", STRING_512),
			new SqlColumn("rewardText", STRING_512), new SqlColumn("hidden", BOOLEAN) };
	private final String serverIconTable = "gcs_Server_MetaData";
	private final SqlColumn[] serverIconTableColumns = new SqlColumn[] { new SqlColumn("server", STRING_64, true),
			new SqlColumn("iconMaterial", STRING_64), new SqlColumn("iconDamage", INT_1_BYTE) };

	private AchievementDatabase(JavaPlugin plugin) {
		super(plugin);
		Bukkit.getPluginManager().registerEvents(this, plugin);

		api.addTable(serverIconTable, serverIconTableColumns);
		api.addTable(achievementMetaTable, achievementMetaColumns);
		api.addTable(getAchTableName(), new SqlColumn("UUID", SqlColumnType.STRING_64, true));
		api.addTable(achievementPointsTable, achievementPointsTablColumns);

		api.addColumn(achievementPointsTable,
				new SqlColumn(GCStatsTrackerConfig.serverName.replaceAll(" ", "_"), SqlColumnType.INT));

	}

	public boolean isConnected() {
		return api.isConnected();
	}

	public void runOnConnect(Runnable r) {
		api.runOnConnect(r);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
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
		String update = "UPDATE " + achievementPointsTable + " SET " + column + " = " + column + " + " + points
				+ " WHERE UUID = '" + p.getName() + "';";
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

		if (values.isEmpty())
			return 0;

		return (Integer.parseInt(values.get(0)[0]));
	}

	void addAchievements(Achievement[] achievements) {
		if (achievements.length == 0)
			return;

		ExecutorService es = Executors.newFixedThreadPool(achievements.length);
		for (Achievement a : achievements) {
			if (a == null)
				continue;
			String update = "REPLACE INTO " + achievementMetaTable
					+ " (server,name,statType,required,points,icon,iconDamage,description,rewardText,hidden) VALUES"
					+ "('" + GCStatsTrackerConfig.serverName.replaceAll(" ", "_") + "','"
					+ a.getName().replaceAll(" ", "_") + "','"
					+ (a instanceof StatAchievement ? ((StatAchievement) a).getStatType().getNameUnderscores() : "null")
					+ "'," + (a instanceof StatAchievement ? ((StatAchievement) a).getRequired() : "0") + ","
					+ a.getPoints() + ",'" + a.getIcon() + "'," + a.getIconDamage() + ",'" + StringUtils.arrayToString(a.getDescription(), 0, "\n") + "','"
					+ StringUtils.arrayToString(a.getRewardText(), 0, "\n") + "'," + a.isHidden() + ");";

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
		}
		catch (InterruptedException e) {
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
			String[] description = s[7].split("\n");
			String[] rewardText = s[8].split("\n");
			boolean hidden = Integer.parseInt(s[9]) != 0;

			if (statType.equalsIgnoreCase("null") || statType.equals(""))
				achievements.add(
						new RemoteAchievement(server, name, points, m, iconDamage, description, rewardText, hidden));
			else
				achievements.add(new RemoteStatAchievement(server, name, points, m, iconDamage, description, rewardText,
						hidden, statType, required));
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
		}
		catch (Exception e) {
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

	private String getAchTableName() {
		return getAchTableName(GCStatsTrackerConfig.serverName);
	}

	private String getAchTableName(String server) {
		return "gcs_achievemnts_" + server.replaceAll(" ", "_");
	}
}
