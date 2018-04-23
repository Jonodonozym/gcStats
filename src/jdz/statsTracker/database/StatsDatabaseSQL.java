package jdz.statsTracker.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import jdz.bukkitUtils.misc.StringUtils;
import jdz.bukkitUtils.sql.SqlColumn;
import jdz.bukkitUtils.sql.SqlColumnType;
import jdz.bukkitUtils.sql.SqlDatabase;
import jdz.bukkitUtils.sql.SqlRow;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.stats.NoSaveStatType;
import jdz.statsTracker.stats.StatType;
import lombok.Getter;

/**
 * Utility class with static methods to interact with the sql database
 * 
 * @author Jonodonozym
 */
class StatsDatabaseSQL extends SqlDatabase implements Listener, StatsDatabase {
	@Getter private static final StatsDatabaseSQL instance = new StatsDatabaseSQL(GCStats.instance);

	private StatsDatabaseSQL(JavaPlugin plugin) {
		super(plugin);
		Bukkit.getPluginManager().registerEvents(this, plugin);
		ensureCorrectTables();
	}

	private final String statsMetaTable = "gcs_Stat_MetaData";

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String update = "INSERT INTO {table} (UUID) " + "SELECT '" + player.getName() + "' FROM dual "
				+ "WHERE NOT EXISTS ( SELECT UUID FROM {table} WHERE UUID = '" + player.getName() + "' ) LIMIT 1;";
		for (String server : GCStatsConfig.servers)
			updateAsync(update.replaceAll("\\{table\\}", getStatTableName(server)));
	}

	public void ensureCorrectTables() {
		ensureCorrectStatMetaTable();
		ensureCorrectStatTable();
	}

	private void ensureCorrectStatMetaTable() {
		String newTable = "CREATE TABLE IF NOT EXISTS " + statsMetaTable + " (server varchar(127));";
		String deleteOldRow = "DELETE FROM " + statsMetaTable + " WHERE server = '"
				+ GCStatsConfig.serverName.replaceAll(" ", "_") + "';";
		String newRow = "INSERT INTO " + statsMetaTable + " (server) " + " VALUES('"
				+ GCStatsConfig.serverName.replaceAll(" ", "_") + "');";
		update(newTable);
		update(deleteOldRow);
		update(newRow);
	}

	private void ensureCorrectStatTable() {
		String update = "CREATE TABLE IF NOT EXISTS " + getStatTableName() + " (UUID varchar(127));";
		update(update);
	}

	@Override
	public int countEntries(String server) {
		if (!isConnected())
			return -1;
		String query = "SELECT COUNT(*) FROM " + getStatTableName(server) + ";";
		SqlRow row = queryFirst(query);
		return Integer.parseInt(row.get(0));
	}

	@Override
	public void addStatType(StatType type, boolean isEnabled) {
		// Stat Meta-data
		String setValue = "UPDATE " + statsMetaTable + " SET {column} = {value} WHERE server = '"
				+ GCStatsConfig.serverName.replaceAll(" ", "_") + "';";

		addColumn(statsMetaTable, new SqlColumn(type.getNameUnderscores() + "_enabled", SqlColumnType.BOOLEAN));
		addColumn(statsMetaTable, new SqlColumn(type.getNameUnderscores() + "_visible", SqlColumnType.BOOLEAN));

		updateAsync(setValue.replaceAll("\\{column\\}", type.getNameUnderscores() + "_enabled")
				.replaceAll("\\{value\\}", isEnabled + ""));
		updateAsync(setValue.replaceAll("\\{column\\}", type.getNameUnderscores() + "_visible")
				.replaceAll("\\{value\\}", type.isVisible() + ""));

		// stat table
		addColumn(getStatTableName(),
				new SqlColumn(type.getNameUnderscores(), SqlColumnType.DOUBLE, type.getDefault() + ""));
	}

	private String getStatTableName() {
		return getStatTableName(GCStatsConfig.serverName);
	}

	private String getStatTableName(String server) {
		return "gcs_stats_" + server.replaceAll(" ", "_");
	}

	@Override
	public List<String> getEnabledStats(String server) {
		List<String> enabledStats = new ArrayList<String>();
		if (!isConnected())
			return enabledStats;
		List<String> columns = getColumns(statsMetaTable);

		String query = "SELECT * FROM " + statsMetaTable + " WHERE server = '" + server.replaceAll(" ", "_") + "';";
		SqlRow row = query(query).get(0);
		int i = 0;
		for (String s : columns) {
			try {
				if (Integer.parseInt(row.get(i++)) == 1)
					if (s.endsWith("_enabled"))
						enabledStats.add(StringUtils.capitalizeWord(s.replaceAll("_enabled", "").replaceAll("_", " ")));
			}
			catch (NumberFormatException e) {}
		}

		Collections.sort(enabledStats);
		return enabledStats;
	}

	@Override
	public List<String> getVisibleStats(String server) {
		List<String> enabledStats = new ArrayList<String>();
		if (!isConnected())
			return enabledStats;
		List<String> columns = getColumns(statsMetaTable);

		String query = "SELECT * FROM " + statsMetaTable + " WHERE server = '" + server.replaceAll(" ", "_") + "';";
		SqlRow row = query(query).get(0);
		int i = 0;
		for (String s : columns) {
			try {
				if (Integer.parseInt(row.get(i++)) == 1)
					if (s.endsWith("_visible"))
						enabledStats.add(StringUtils.capitalizeWord(s.replaceAll("_visible", "").replaceAll("_", " ")));
			}
			catch (NumberFormatException e) {}
		}

		Collections.sort(enabledStats);
		return enabledStats;
	}

	@Override
	public boolean hasPlayer(OfflinePlayer player, String server) {
		if (!isConnected())
			return false;
		return queryFirst(
				"SELECT UUID FROM " + getStatTableName(server) + " WHERE UUID = '" + player.getName() + "';") != null;
	}

	@Override
	public void setStat(OfflinePlayer player, StatType statType, double newValue) {
		String update = "UPDATE " + getStatTableName() + " SET " + statType.getNameUnderscores() + " = " + newValue
				+ " WHERE UUID = '" + player.getName() + "';";
		updateAsync(update);
	}

	@Override
	public void addStat(OfflinePlayer player, StatType statType, double change) {
		if (statType instanceof NoSaveStatType)
			return;
		String update = "UPDATE " + getStatTableName() + " SET " + statType.getNameUnderscores() + " = "
				+ statType.getNameUnderscores() + " + " + change + " WHERE UUID = '" + player.getName() + "';";
		updateAsync(update);
	}

	@Override
	public void setStatSync(OfflinePlayer player, StatType statType, double newValue) {
		if (statType instanceof NoSaveStatType)
			return;
		String update = "UPDATE " + getStatTableName() + " SET " + statType.getNameUnderscores() + " = " + newValue
				+ " WHERE UUID = '" + player.getName() + "';";
		update(update);
	}

	@Override
	public double getStat(OfflinePlayer player, StatType statType) {
		if (statType instanceof NoSaveStatType)
			return statType.getDefault();
		return getStat(player, statType.getNameUnderscores(), GCStatsConfig.serverName.replaceAll(" ", "_"));
	}

	/**
	 * Warning: really slow! Use async or StatType.getInstance().get(player)
	 * 
	 * @param player
	 * @param statType
	 * @param server
	 * @return
	 */
	@Override
	public double getStat(OfflinePlayer player, String statType, String server) {
		if (!isConnected())
			return 0;

		String query = "SELECT " + statType.replaceAll(" ", "_") + " FROM " + getStatTableName(server) + " WHERE UUID = '" + player.getName()
				+ "';";
		List<SqlRow> rows = query(query);

		if (rows.isEmpty())
			return 0;

		return Double.parseDouble(rows.get(0).get(0));
	}

	public int getNumRows() {
		if (!isConnected())
			return 0;

		String query = "Select COUNT(*) FROM " + getStatTableName() + ";";
		return Integer.parseInt(query(query).get(0).get(0));
	}

	@Override
	public Map<String, Double> getAllSorted(StatType type) {
		String query = "Select UUID, " + type.getNameUnderscores() + " FROM " + getStatTableName() + " ORDER BY "
				+ type.getNameUnderscores() + " DESC;";
		LinkedHashMap<String, Double> result = new LinkedHashMap<String, Double>();
		for (SqlRow row : query(query))
			result.put(row.get(0), Double.parseDouble(row.get(1)));
		return result;
	}

	@Override
	public void onShutDown() {}
}
