
package jdz.statsTracker.database;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import jdz.bukkitUtils.sql.SqlColumn;
import jdz.bukkitUtils.sql.SqlDatabase;
import jdz.bukkitUtils.sql.SqlRow;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.achievement.Achievement;
import jdz.statsTracker.achievement.AchievementInventories;
import jdz.statsTracker.achievement.AchievementShop;
import jdz.statsTracker.achievement.RemoteAchievement;
import jdz.statsTracker.achievement.RemoteStatAchievement;
import jdz.statsTracker.achievement.StatAchievement;

import static jdz.bukkitUtils.sql.SqlColumnType.*;

import lombok.Getter;

class AchievementDatabaseSQL extends SqlDatabase implements AchievementDatabase, Listener {
	@Getter private static final AchievementDatabaseSQL instance = new AchievementDatabaseSQL(GCStats.instance);

	protected String achievementPointsTable = "gcs_Achievement_Points";
	protected SqlColumn[] achievementPointsTablColumns = new SqlColumn[] { new SqlColumn("UUID", STRING_64, true),
			new SqlColumn("Global", INT) };
	private String achievementMetaTable = "gcs_Achievement_MetaData";
	protected SqlColumn[] achievementMetaColumns = new SqlColumn[] { new SqlColumn("server", STRING_64, true),
			new SqlColumn("name", STRING_128, true), new SqlColumn("statType", STRING_64),
			new SqlColumn("required", DOUBLE), new SqlColumn("points", INT), new SqlColumn("icon", STRING_32),
			new SqlColumn("iconDamage", INT), new SqlColumn("iconQuantity", INT_2_BYTE, "1"),
			new SqlColumn("description", STRING_512), new SqlColumn("rewardText", STRING_512),
			new SqlColumn("hidden", BOOLEAN), new SqlColumn("newLineAfter", BOOLEAN),
			new SqlColumn("newLineBefore", BOOLEAN) };
	protected String serverIconTable = "gcs_Server_MetaData";
	protected SqlColumn[] serverIconTableColumns = new SqlColumn[] { new SqlColumn("server", STRING_64, true),
			new SqlColumn("iconMaterial", STRING_64), new SqlColumn("iconDamage", INT_1_BYTE) };

	private AchievementDatabaseSQL(JavaPlugin plugin) {
		super(plugin);
		Bukkit.getPluginManager().registerEvents(this, plugin);

		Bukkit.getScheduler().runTaskLater(GCStats.getInstance(), () -> {
			runOnConnect(() -> {
				GCStatsConfig.servers = getServers();
				setServerIcon(GCStatsConfig.serverName, GCStatsConfig.serverIcon, GCStatsConfig.serverIconData);

				addTable(serverIconTable, serverIconTableColumns);
				addTable(achievementMetaTable, achievementMetaColumns);
				addTable(getAchTableName(), new SqlColumn("UUID", STRING_64, true));
				addTable(achievementPointsTable, achievementPointsTablColumns);

				addColumn(achievementPointsTable, new SqlColumn(GCStatsConfig.serverName.replaceAll(" ", "_"), INT));

				AchievementInventories.getInstance().reload();
				AchievementShop.reload();
			});
		}, 1L);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		String update = "INSERT INTO {table} (UUID) " + "SELECT '" + player.getName() + "' FROM dual "
				+ "WHERE NOT EXISTS ( SELECT UUID FROM {table} WHERE UUID = '" + player.getName() + "' ) LIMIT 1;";
		updateAsync(update.replaceAll("\\{table\\}", achievementPointsTable));
		updateAsync(update.replaceAll("\\{table\\}", getAchTableName()));
	}

	@Override
	public void setAchievementPoints(Player player, int points) {
		String column = GCStatsConfig.achievementPointsGlobal ? "Global"
				: GCStatsConfig.serverName.replaceAll(" ", "_");
		String update = "UPDATE " + achievementPointsTable + " SET " + column + " = " + points + " WHERE UUID = '"
				+ player.getName() + "';";
		updateAsync(update);
	}

	@Override
	public void addAchievementPoints(Player player, int points) {
		String column = GCStatsConfig.achievementPointsGlobal ? "Global"
				: GCStatsConfig.serverName.replaceAll(" ", "_");
		String update = "UPDATE " + achievementPointsTable + " SET " + column + " = " + column + " + " + points
				+ " WHERE UUID = '" + player.getName() + "';";
		updateAsync(update);
	}

	@Override
	public int getAchievementPoints(OfflinePlayer player) {
		return (getAchievementPoints(player, GCStatsConfig.serverName.replaceAll(" ", "_")));
	}

	@Override
	public int getAchievementPoints(OfflinePlayer player, String server) {
		if (!isConnected())
			return 0;

		String query = "SELECT " + server + " FROM " + achievementPointsTable + " WHERE UUID = '" + player.getName()
				+ "';";
		List<SqlRow> rows = query(query);

		if (rows.isEmpty())
			return 0;

		return (Integer.parseInt(rows.get(0).get(server)));
	}

	@Override
	public void addAchievements(Achievement[] achievements) {
		if (achievements.length == 0)
			return;

		ExecutorService es = Executors.newFixedThreadPool(achievements.length);
		for (Achievement a : achievements) {
			if (a == null)
				continue;
			String update = "REPLACE INTO " + achievementMetaTable
					+ " (server,name,statType,required,points,icon,iconDamage,iconQuantity,description,rewardText,hidden,newLineAfter,newLineBefore) VALUES"
					+ "('" + GCStatsConfig.serverName.replaceAll(" ", "_") + "','" + a.getName().replaceAll(" ", "_")
					+ "','"
					+ (a instanceof StatAchievement ? ((StatAchievement) a).getStatType().getNameUnderscores() : "null")
					+ "'," + (a instanceof StatAchievement ? ((StatAchievement) a).getRequired() : "0") + ","
					+ a.getPoints() + ",'" + a.getIcon() + "'," + a.getIconDamage() + ","+a.getIconQuantity()+",'"
					+ StringUtils.arrayToString(a.getDescription(), 0, "\n") + "','"
					+ StringUtils.arrayToString(a.getRewardText(), 0, "\n") + "'," + a.isHidden() + ","
					+ a.isNewLineAfter() + "," + a.isNewLineBefore() + ");";

			es.execute(() -> {
				update(update);
			});
		}

		for (Achievement a : achievements) {
			if (a == null)
				continue;
			es.execute(() -> {
				addColumn(getAchTableName(), new SqlColumn(a.getName().replace(' ', '_'), BOOLEAN));
			});
		}

		es.shutdown();
		try {
			es.awaitTermination(1, TimeUnit.MINUTES);
		}
		catch (InterruptedException e) {
			new FileLogger(GCStats.instance).createErrorLog(e);
		}
	}

	@Override
	public List<Achievement> getServerAchievements(String server) {
		List<Achievement> achievements = new ArrayList<Achievement>();

		if (!isConnected())
			return achievements;

		String query = "SELECT * FROM " + achievementMetaTable + " WHERE server = '" + server.replaceAll(" ", "_")
				+ "';";
		List<SqlRow> rows = query(query);
		for (SqlRow row : rows) {
			String name = row.get(achievementMetaColumns[1]);
			String statType = row.get(achievementMetaColumns[2]);
			double required = Double.parseDouble(row.get(achievementMetaColumns[3]));
			int points = Integer.parseInt(row.get(achievementMetaColumns[4]));
			Material m = Material.valueOf(row.get(achievementMetaColumns[5]));
			short iconDamage = Short.parseShort(row.get(achievementMetaColumns[6]));
			int iconQuantity = Integer.parseInt(row.get(achievementMetaColumns[7]));
			String[] description = row.get(achievementMetaColumns[8]).split("\n");
			String[] rewardText = row.get(achievementMetaColumns[9]).split("\n");
			boolean hidden = Integer.parseInt(row.get(achievementMetaColumns[10])) != 0;
			boolean isNewLineAfter = Integer.parseInt(row.get(achievementMetaColumns[11])) != 0;
			boolean isNewLineBefore = Integer.parseInt(row.get(achievementMetaColumns[12])) != 0;

			RemoteAchievement a;
			if (statType.equalsIgnoreCase("null") || statType.equals(""))
				a = new RemoteAchievement(server, name, points, m, iconDamage, description, rewardText, hidden);
			else
				a = new RemoteStatAchievement(server, name, points, m, iconDamage, description, rewardText, hidden,
						statType, required);
			a.setNewLineAfter(isNewLineAfter);
			a.setNewLineBefore(isNewLineBefore);
			a.setIconQuantity(iconQuantity);

			achievements.add(a);
		}
		return achievements;
	}

	@Override
	public boolean isAchieved(OfflinePlayer offlinePlayer, Achievement a) {
		return isAchieved(offlinePlayer, a, GCStatsConfig.serverName);
	}

	@Override
	public boolean isAchieved(OfflinePlayer offlinePlayer, Achievement a, String server) {
		if (!isConnected())
			return true;
		String query = "SELECT " + a.getName().replace(' ', '_') + " FROM " + getAchTableName(server)
				+ " WHERE UUID = '" + offlinePlayer.getName() + "';";
		try {
			return Integer.parseInt(query(query).get(0).get(0)) == 1;
		}
		catch (Exception e) {
			return true;
		}
	}

	@Override
	public void setAchieved(Player player, Achievement a) {
		String update = "UPDATE " + getAchTableName() + " SET " + a.getName().replace(' ', '_')
				+ " = true WHERE UUID = '" + player.getName() + "';";
		updateAsync(update);
	}

	@Override
	public List<String> getServers() {
		if (!isConnected())
			return new ArrayList<String>();
		List<String> columns = getColumns(achievementPointsTable);
		columns.remove("UUID");
		columns.remove("Global");
		List<String> servers = new ArrayList<String>();
		for (String s : columns)
			servers.add(s.replaceAll("_", " "));
		return servers;
	}

	@Override
	public boolean hasServer(String server) {
		return (getServers().contains(server));
	}

	@Override
	public void setServerIcon(String server, Material m, short damage) {
		if (!isConnected())
			return;
		String update = "REPLACE INTO " + serverIconTable + " (server, iconMaterial, iconDamage) values('"
				+ server.replaceAll(" ", "_") + "','" + m + "'," + damage + ");";
		update(update);
	}

	@Override
	public ItemStack getServerIcon(String server) {
		if (!isConnected())
			return new ItemStack(Material.STONE);
		String query = "SELECT iconMaterial, iconDamage FROM " + serverIconTable + " WHERE server = '"
				+ server.replaceAll(" ", "_") + "';";
		List<SqlRow> list = query(query);
		Material m = Material.valueOf(list.get(0).get(0));
		short damage = Short.parseShort(list.get(0).get(1));
		ItemStack is = new ItemStack(m, 1, damage);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.GREEN + server.replaceAll("_", " "));
		is.setItemMeta(im);
		return is;
	}

	private String getAchTableName() {
		return getAchTableName(GCStatsConfig.serverName);
	}

	private String getAchTableName(String server) {
		return "gcs_achievemnts_" + server.replaceAll(" ", "_");
	}
}
