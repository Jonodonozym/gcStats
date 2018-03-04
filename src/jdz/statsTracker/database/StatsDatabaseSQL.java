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
import jdz.bukkitUtils.sql.Database;
import jdz.bukkitUtils.sql.SqlColumn;
import jdz.bukkitUtils.sql.SqlColumnType;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.stats.StatType;
import lombok.Getter;

/**
 * Utility class with static methods to interact with the sql database
 * 
 * @author Jonodonozym
 */
class StatsDatabaseSQL extends Database implements Listener, StatsDatabase {
	@Getter private static final StatsDatabaseSQL instance = new StatsDatabaseSQL(GCStats.instance);

	private StatsDatabaseSQL(JavaPlugin plugin) {
		super(plugin);
		Bukkit.getPluginManager().registerEvents(StatsDatabaseSQL.getInstance(), plugin);
		api.runOnConnect(() -> {
			ensureCorrectTables();
		});
	}

	private final String statsMetaTable = "gcs_Stat_MetaData";

	public void runOnConnect(Runnable r) {
		api.runOnConnect(r);
	}

	public boolean isConnected() {
		return api.isConnected();
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String update = "INSERT INTO {table} (UUID) " + "SELECT '" + player.getName() + "' FROM dual "
				+ "WHERE NOT EXISTS ( SELECT UUID FROM {table} WHERE UUID = '" + player.getName() + "' ) LIMIT 1;";
		for (String server : GCStatsConfig.servers)
			api.executeUpdateAsync(update.replaceAll("\\{table\\}", getStatTableName(server)));
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
		api.executeUpdate(newTable);
		api.executeUpdate(deleteOldRow);
		api.executeUpdate(newRow);
	}

	private void ensureCorrectStatTable() {
		String update = "CREATE TABLE IF NOT EXISTS " + getStatTableName() + " (UUID varchar(127));";
		api.executeUpdate(update);
	}

	@Override
	public void addStatType(StatType type, boolean isEnabled) {
		// Stat Meta-data
		String setValue = "UPDATE " + statsMetaTable + " SET {column} = {value} WHERE server = '"
				+ GCStatsConfig.serverName.replaceAll(" ", "_") + "';";

		api.addColumn(statsMetaTable, new SqlColumn(type.getNameUnderscores() + "_enabled", SqlColumnType.BOOLEAN));
		api.addColumn(statsMetaTable, new SqlColumn(type.getNameUnderscores() + "_visible", SqlColumnType.BOOLEAN));

		api.executeUpdateAsync(setValue.replaceAll("\\{column\\}", type.getNameUnderscores() + "_enabled")
				.replaceAll("\\{value\\}", isEnabled + ""));
		api.executeUpdateAsync(setValue.replaceAll("\\{column\\}", type.getNameUnderscores() + "_visible")
				.replaceAll("\\{value\\}", type.isVisible() + ""));

		// stat table
		api.addColumn(getStatTableName(),
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
		if (!api.isConnected())
			return enabledStats;
		List<String> columns = api.getColumns(statsMetaTable);

		String query = "SELECT * FROM " + statsMetaTable + " WHERE server = '" + server.replaceAll(" ", "_") + "';";
		String[] row = api.getRows(query).get(0);
		int i = 0;
		for (String s : columns) {
			try {
				if (Integer.parseInt(row[i++]) == 1)
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
		if (!api.isConnected())
			return enabledStats;
		List<String> columns = api.getColumns(statsMetaTable);

		String query = "SELECT * FROM " + statsMetaTable + " WHERE server = '" + server.replaceAll(" ", "_") + "';";
		String[] row = api.getRows(query).get(0);
		int i = 0;
		for (String s : columns) {
			try {
				if (Integer.parseInt(row[i++]) == 1)
					if (s.endsWith("_visible"))
						enabledStats.add(StringUtils.capitalizeWord(s.replaceAll("_visible", "").replaceAll("_", " ")));
			}
			catch (NumberFormatException e) {}
		}

		Collections.sort(enabledStats);
		return enabledStats;
	}

	@Override
	public void setStat(OfflinePlayer player, StatType statType, double newValue) {
		String update = "UPDATE " + getStatTableName() + " SET " + statType.getNameUnderscores() + " = " + newValue
				+ " WHERE UUID = '" + player.getName() + "';";
		api.executeUpdateAsync(update);
	}

	@Override
	public void addStat(OfflinePlayer player, StatType statType, double change) {
		String update = "UPDATE " + getStatTableName() + " SET " + statType.getNameUnderscores() + " = "
				+ statType.getNameUnderscores() + " + " + change + " WHERE UUID = '" + player.getName() + "';";
		api.executeUpdateAsync(update);
	}

	@Override
	public void setStatSync(OfflinePlayer player, StatType statType, double newValue) {
		String update = "UPDATE " + getStatTableName() + " SET " + statType.getNameUnderscores() + " = " + newValue
				+ " WHERE UUID = '" + player.getName() + "';";
		api.executeUpdate(update);
	}

	@Override
	public double getStat(OfflinePlayer player, StatType statType) {
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
		if (!api.isConnected())
			return 0;

		String query = "SELECT " + statType + " FROM " + getStatTableName(server) + " WHERE UUID = '" + player.getName()
				+ "';";
		List<String[]> values = api.getRows(query);

		if (values.isEmpty())
			return 0;

		return Double.parseDouble(values.get(0)[0]);
	}

	public int getNumRows() {
		if (!api.isConnected())
			return 0;

		String query = "Select COUNT(*) FROM " + getStatTableName() + ";";
		return Integer.parseInt(api.getRows(query).get(0)[0]);
	}

	@Override
	public Map<String, Double> getAllSorted(StatType type) {
		String query = "Select UUID, " + type.getNameUnderscores() + " FROM " + getStatTableName() + " ORDER BY "
				+ type.getNameUnderscores() + " DESC;";
		LinkedHashMap<String, Double> result = new LinkedHashMap<String, Double>();
		for (String[] row : api.getRows(query))
			result.put(row[0], Double.parseDouble(row[1]));
		return result;
	}

	@Override
	public void onShutDown() {}
}
