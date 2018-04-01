
package jdz.statsTracker.objective;

import org.bukkit.plugin.Plugin;

import jdz.statsTracker.stats.NoSaveStatType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StatObjective extends AbstractObjective {
	private final NoSaveStatType statType;
	private final double required;

	public StatObjective(String description, NoSaveStatType statType, double required) {
		this("", description, statType, required);
	}
	
	public StatObjective(String name, String description, NoSaveStatType statType, double required) {
		super(name, description);
		this.statType = statType;
		this.required = required;
	}
	
	public void register(Plugin plugin) {
		ObjectiveManager.registerObjective(plugin, this);
	}
}
