package jdz.statsTracker.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import jdz.bukkitUtils.sql.Database;
import jdz.statsTracker.GCStatsTracker;
import jdz.statsTracker.GCStatsTrackerConfig;
import jdz.statsTracker.achievement.AchievementDatabase;

/**
 * Utility class with static methods to interact with the sql database
 * 
 * @author Jonodonozym
 */
public class StatsDatabase extends Database implements Listener {
	private static final StatsDatabase instance = new StatsDatabase(GCStatsTracker.instance);

	public static StatsDatabase getInstance() {
		return instance;
	}

	private final Map<Player, Map<StatType, Double>> bufferedStats = new HashMap<Player, Map<StatType, Double>>();

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

	public double getStat(OfflinePlayer player, StatType statType) {
		try {
			if (player.isOnline())
				return bufferedStats.get(player.getPlayer()).get(statType);
			return getStatDirect(player, statType);
		} catch (NullPointerException e) {
			return getStatDirect(player, statType);
		}
	}

	public void setStat(Player player, StatType stat, double newValue) {
		bufferedStats.get(player).put(stat, newValue);
	}

	public void addStat(Player player, StatType stat, double change) {
		bufferedStats.get(player).put(stat, getStat(player, stat) + change);
	}

	public List<String> getServers() {
		return AchievementDatabase.getInstance().getServers();
	}

	public boolean hasServer(String server) {
		return getServers().contains(server);
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

		String columnsAddBoolean = "ALTER TABLE " + statsMetaTable + " ADD COLUMN {column} Boolean NOT NULL default 0";
		String setValue = "UPDATE " + statsMetaTable + " SET {column} = {value} WHERE server = '"
				+ GCStatsTrackerConfig.serverName.replaceAll(" ", "_") + "';";
		Set<String> columns = new HashSet<String>();
		columns.addAll(api.getColumns(statsMetaTable));
		for (StatType s : StatType.values()) {
			if (!columns.contains(s.toString()))
				api.executeUpdate(columnsAddBoolean.replaceAll("\\{column\\}", s.toString()));
			api.executeUpdate(setValue.replaceAll("\\{column\\}", s.toString()).replaceAll("\\{value\\}",
					GCStatsTrackerConfig.enabledStats.contains(s) + ""));
		}
	}

	private void ensureCorrectStatTable() {
		String update = "CREATE TABLE IF NOT EXISTS " + getStatTableName() + " (UUID varchar(127));";
		api.executeUpdate(update);

		String columnsAdd = "ALTER TABLE " + getStatTableName() + " ADD COLUMN {column} DOUBLE DEFAULT 0";

		List<String> columns = api.getColumns(getStatTableName());

		for (StatType s : StatType.values())
			if (GCStatsTrackerConfig.enabledStats.contains(s))
				if (!columns.contains(s.toString()))
					api.executeUpdate(columnsAdd.replaceAll("\\{column\\}", s.toString()));
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
					enabledStats.add(s);
			} catch (NumberFormatException e) {
			}
		}

		return enabledStats;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		onPlayerJoin(e.getPlayer());
	}
	
	public void onPlayerJoin(Player p) {
		String update = "INSERT INTO {table} (UUID) " + "SELECT '" + p.getName() + "' FROM dual "
				+ "WHERE NOT EXISTS ( SELECT UUID FROM {table} WHERE UUID = '" + p.getName()
				+ "' ) LIMIT 1;";
		for (String server : getServers())
			api.executeUpdateAsync(update.replaceAll("\\{table\\}", getStatTableName(server)));

		bufferedStats.put(p, new HashMap<StatType, Double>());
		new BukkitRunnable() {
			@Override
			public void run() {
				for (StatType statType: GCStatsTrackerConfig.enabledStats)
					bufferedStats.get(p).put(statType, getStatDirect(p, statType));
			}
		}.runTaskAsynchronously(GCStatsTracker.instance);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		onPlayerQuit(e.getPlayer());
	}
	
	public void onPlayerQuit(Player p) {
		addStat(p, StatType.PLAY_TIME, 
				(System.currentTimeMillis() - PlayTimeRecorder.getInstance().lastTime.get(p))/1000);
		setStat(p, StatType.DIST_WALKED, p.getStatistic(Statistic.WALK_ONE_CM)/100.0);
		
		for (StatType statType: GCStatsTrackerConfig.enabledStats)
			setStatDirect(p, statType, bufferedStats.get(p).get(statType));
		bufferedStats.remove(p);
	}
	
	public void onShutDown() {
		for (Player p: Bukkit.getOnlinePlayers()) {
			addStat(p, StatType.PLAY_TIME, 
					(System.currentTimeMillis() - PlayTimeRecorder.getInstance().lastTime.get(p))/1000);
			setStat(p, StatType.DIST_WALKED, p.getStatistic(Statistic.WALK_ONE_CM)/100.0);
			
			for (StatType statType: GCStatsTrackerConfig.enabledStats)
				setStatDirectSync(p, statType, bufferedStats.get(p).get(statType));
		}
	}
	
	private void setStatDirect(Player player, StatType statType, double newValue) {
		String update = "UPDATE " + getStatTableName() + " SET " + statType + " = " + newValue + " WHERE UUID = '"
				+ player.getName() + "';";
		api.executeUpdateAsync(update);
	}
	
	private void setStatDirectSync(Player player, StatType statType, double newValue) {
		String update = "UPDATE " + getStatTableName() + " SET " + statType + " = " + newValue + " WHERE UUID = '"
				+ player.getName() + "';";
		api.executeUpdate(update);
	}
	
	public double getStatDirect(OfflinePlayer player, StatType statType) {
		return getStatDirect(player, statType.name(), GCStatsTrackerConfig.serverName.replaceAll(" ", "_"));
	}

	public double getStatDirect(OfflinePlayer player, String statType, String server) {
		if (!api.isConnected())
			return 0;
		
		String query = "SELECT " + statType + " FROM " + getStatTableName(server) + " WHERE UUID = '"
				+ player.getName() + "';";
		List<String[]> values = api.getRows(query);
		
		try {
			return Double.parseDouble(values.get(0)[0]);
		}
		catch (Exception e) {
			return 0;
		}
	}
}
