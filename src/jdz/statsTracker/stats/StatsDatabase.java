package jdz.statsTracker.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import jdz.bukkitUtils.misc.StringUtils;
import jdz.bukkitUtils.sql.Database;
import jdz.statsTracker.GCStatsTracker;
import jdz.statsTracker.GCStatsTrackerConfig;
import lombok.Getter;

/**
 * Utility class with static methods to interact with the sql database
 * 
 * @author Jonodonozym
 */
public class StatsDatabase extends Database {
	@Getter
	private static final StatsDatabase instance = new StatsDatabase(GCStatsTracker.instance);

	private StatsDatabase(JavaPlugin plugin) {
		super(plugin);
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

	void addPlayer(Player player) {
		String update = "INSERT INTO {table} (UUID) " + "SELECT '" + player.getName() + "' FROM dual "
				+ "WHERE NOT EXISTS ( SELECT UUID FROM {table} WHERE UUID = '" + player.getName() + "' ) LIMIT 1;";
		for (String server : GCStatsTrackerConfig.servers)
			api.executeUpdateAsync(update.replaceAll("\\{table\\}", getStatTableName(server)));
	}

	public void ensureCorrectTables() {
		ensureCorrectStatMetaTable();
		ensureCorrectStatTable();
	}

	private void ensureCorrectStatMetaTable() {
		String newTable = "CREATE TABLE IF NOT EXISTS " + statsMetaTable + " (server varchar(127));";
		String newRow = "INSERT INTO " + statsMetaTable + " (server) " + "SELECT '"
				+ GCStatsTrackerConfig.serverName.replaceAll(" ", "_") + "' FROM dual "
				+ "WHERE NOT EXISTS ( SELECT server FROM " + statsMetaTable + " WHERE server = '"
				+ GCStatsTrackerConfig.serverName.replaceAll(" ", "_") + "' ) LIMIT 1;";
		api.executeUpdate(newTable);
		api.executeUpdate(newRow);
	}

	private void ensureCorrectStatTable() {
		String update = "CREATE TABLE IF NOT EXISTS " + getStatTableName() + " (UUID varchar(127));";
		api.executeUpdate(update);
	}

	public void addStatType(StatType type) {
		// Stat Meta-data
		String columnsAddBoolean = "ALTER TABLE " + statsMetaTable + " ADD COLUMN {column} Boolean NOT NULL default 0";
		String setValue = "UPDATE " + statsMetaTable + " SET {column} = {value} WHERE server = '"
				+ GCStatsTrackerConfig.serverName.replaceAll(" ", "_") + "';";
		List<String> columns = api.getColumns(statsMetaTable);

		if (!columns.contains(type.getNameUnderscores()))
			api.executeUpdate(columnsAddBoolean.replaceAll("\\{column\\}", type.getNameUnderscores()));
		api.executeUpdate(
				setValue.replaceAll("\\{column\\}", type.getNameUnderscores()).replaceAll("\\{value\\}", "true"));

		// stat table
		String columnsAdd = "ALTER TABLE " + getStatTableName() + " ADD COLUMN {column} DOUBLE DEFAULT 0";

		columns = api.getColumns(getStatTableName());

		if (!columns.contains(type.getNameUnderscores()))
			api.executeUpdate(columnsAdd.replaceAll("\\{column\\}", type.getNameUnderscores()));
	}

	private String getStatTableName() {
		return getStatTableName(GCStatsTrackerConfig.serverName);
	}

	private String getStatTableName(String server) {
		return "gcs_stats_" + server.replaceAll(" ", "_");
	}

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
					enabledStats.add(StringUtils.capitalizeWord(s.replaceAll("_", " ")));
			} catch (NumberFormatException e) {
			}
		}

		Collections.sort(enabledStats);
		return enabledStats;
	}

	public void setStat(Player player, StatType statType, double newValue) {
		String update = "UPDATE " + getStatTableName() + " SET " + statType.getNameUnderscores() + " = " + newValue
				+ " WHERE UUID = '" + player.getName() + "';";
		api.executeUpdateAsync(update);
	}

	public void setStatSync(Player player, StatType statType, double newValue) {
		String update = "UPDATE " + getStatTableName() + " SET " + statType.getNameUnderscores() + " = " + newValue
				+ " WHERE UUID = '" + player.getName() + "';";
		api.executeUpdate(update);
	}

	double getStat(OfflinePlayer player, StatType statType) {
		return getStat(player, statType.getNameUnderscores(), GCStatsTrackerConfig.serverName.replaceAll(" ", "_"));
	}

	/**
	 * Warning: really slow! Use async or StatType.getInstance().get(player)
	 * 
	 * @param player
	 * @param statType
	 * @param server
	 * @return
	 */
	public double getStat(OfflinePlayer player, String statType, String server) {
		if (!api.isConnected())
			return 0;

		String query = "SELECT " + statType + " FROM " + getStatTableName(server) + " WHERE UUID = '" + player.getName()
				+ "';";
		List<String[]> values = api.getRows(query);

		try {
			return Double.parseDouble(values.get(0)[0]);
		} catch (Exception e) {
			return 0;
		}
	}
	
	public int getNumRows() {
		String query = "Select COUNT(*) FROM "+getStatTableName()+";";
		return Integer.parseInt(api.getRows(query).get(0)[0]);
	}

	public List<String[]> getAllSorted(StatType type) {
		String query = "Select " + type.getNameUnderscores() + ", UUID FROM " + getStatTableName() + " ORDER BY "
				+ type.getNameUnderscores() + " DESC;";
		return api.getRows(query);
	}
}
