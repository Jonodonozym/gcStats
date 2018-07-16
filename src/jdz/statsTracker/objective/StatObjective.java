
package jdz.statsTracker.objective;

import jdz.statsTracker.stats.abstractTypes.NoSaveStatType;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = false, of = { "required", "statType" })
public class StatObjective extends AbstractObjective {
	@Getter private final NoSaveStatType statType;
	@Getter private final double required;

	public StatObjective(String description, NoSaveStatType statType, double required) {
		this(statType.getName() + ":" + required, description, statType, required);
	}

	public StatObjective(String name, String description, NoSaveStatType statType, double required) {
		super(name, description);
		this.statType = statType;
		this.required = required;
	}
}
