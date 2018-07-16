package jdz.statsTracker.stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import jdz.bukkitUtils.fileIO.FileLogger;
import jdz.bukkitUtils.misc.StringUtils;
import jdz.bukkitUtils.sql.SQLColumn;
import jdz.bukkitUtils.sql.SQLColumnType;
import jdz.bukkitUtils.sql.SqlDatabase;
import jdz.bukkitUtils.sql.SQLRow;
import jdz.statsTracker.GCStats;
import jdz.statsTracker.GCStatsConfig;
import jdz.statsTracker.stats.abstractTypes.NoSaveStatType;
import lombok.Getter;

/**
 * Utility class with static methods to interact with the sql database
 * 
 * @author Jonodonozym
 */
public class StatsDatabaseSQL extends SqlDatabase implements Listener, StatsDatabase {
	@Getter private static final StatsDatabaseSQL instance = new StatsDatabaseSQL(GCStats.getInstance());
	private final FileLogger logger = new FileLogger(GCStats.getInstance(), "SQL", false);

	private StatsDatabaseSQL(JavaPlugin plugin) {
		super(plugin);
		Bukkit.getPluginManager().registerEvents(this, plugin);
		ensureCorrectTables();
	}

	@Override
	public void runOnConnect(Runnable r) {
		super.runOnConnect(r);
	}

	private final String statsMetaTable = "gcs_Stat_MetaData";

	@Override
	public void addPlayer(OfflinePlayer player) {
		String update = "INSERT INTO %s (UUID) VALUES('%s');";
		update(String.format(update, getStatTableName(), player.getName()));
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
		addTable(getStatTableName(), new SQLColumn("UUID", SQLColumnType.STRING_128, true));
	}

	@Override
	public int countEntries(String server) {
		if (!isConnected())
			return -1;
		String query = "SELECT COUNT(*) FROM " + getStatTableName(server) + ";";
		SQLRow row = queryFirst(query);
		return Integer.parseInt(row.get(0));
	}

	@Override
	public void addStatType(StatType type, boolean isEnabled) {
		// Stat Meta-data
		String setValue = "UPDATE " + statsMetaTable + " SET {column} = {value} WHERE server = '"
				+ GCStatsConfig.serverName.replaceAll(" ", "_") + "';";

		addColumn(statsMetaTable, new SQLColumn(type.getNameUnderscores() + "_enabled", SQLColumnType.BOOLEAN));
		addColumn(statsMetaTable, new SQLColumn(type.getNameUnderscores() + "_visible", SQLColumnType.BOOLEAN));

		updateAsync(setValue.replaceAll("\\{column\\}", type.getNameUnderscores() + "_enabled")
				.replaceAll("\\{value\\}", isEnabled + ""));
		updateAsync(setValue.replaceAll("\\{column\\}", type.getNameUnderscores() + "_visible")
				.replaceAll("\\{value\\}", type.isVisible() + ""));

		// stat table
		addColumn(getStatTableName(),
				new SQLColumn(type.getNameUnderscores(), SQLColumnType.DOUBLE, type.getDefault() + ""));
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
		SQLRow row = query(query).get(0);
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
		SQLRow row = query(query).get(0);
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

		SQLRow row = queryFirst(
				"SELECT UUID FROM " + getStatTableName(server) + " WHERE UUID = '" + player.getName() + "';");
		return row != null;
	}

	@Override
	public void setStats(OfflinePlayer player, Map<StatType, Double> statToValue) {
		String setOperations = "SET ";
		for (StatType type : statToValue.keySet()) {
			if (type instanceof NoSaveStatType)
				continue;
			setOperations += type.getNameUnderscores() + " = " + statToValue.get(type) + ", ";
		}
		if (setOperations.length() == 4)
			return;
		setOperations = setOperations.substring(0, setOperations.length() - 2);
		String update = "UPDATE " + getStatTableName() + " " + setOperations + " WHERE UUID = '" + player.getName()
				+ "';";
		logger.log(update);
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
	public Map<StatType, Double> getStats(OfflinePlayer player, Collection<? extends StatType> statTypes) {
		List<String> types = new ArrayList<String>();
		for (StatType type : statTypes)
			types.add(type.getNameUnderscores());
		Map<String, Double> nameToValue = getStats(player, types, GCStatsConfig.serverName.replaceAll(" ", "_"));
		Map<StatType, Double> typeToValue = new HashMap<StatType, Double>();
		for (StatType type : statTypes)
			typeToValue.put(type, nameToValue.get(type.getNameUnderscores()));
		return typeToValue;
	}

	@Override
	public Map<String, Double> getStats(OfflinePlayer player, Collection<String> statTypes, String server) {
		if (!isConnected())
			throw new RuntimeException("Not connected to the database!");

		String typesList = StringUtils.collectionToString(statTypes, ", ");
		String query = "SELECT " + typesList + " FROM " + getStatTableName(server) + " WHERE UUID = '"
				+ player.getName() + "';";
		logger.log(query);

		List<SQLRow> rows = query(query);

		if (rows.isEmpty())
			throw new RuntimeException("No row exists in the database for '" + player.getName() + "'");
		
		logger.log(rows.get(0).toString());

		Map<String, Double> stats = new HashMap<String, Double>();
		for (String type : statTypes)
			stats.put(type, Double.parseDouble(rows.get(0).get(type)));
		return stats;
	}

	@Override
	public Map<String, Double> getAllSorted(StatType type) {
		String query = "Select UUID, " + type.getNameUnderscores() + " FROM " + getStatTableName() + ";";
		LinkedHashMap<String, Double> result = new LinkedHashMap<String, Double>();
		List<SQLRow> rows = query(query);
		for (SQLRow row : rows)
			result.put(row.get(0), Double.parseDouble(row.get(1)));
		return result.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
						LinkedHashMap::new));
	}
}
