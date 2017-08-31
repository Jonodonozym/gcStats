
package jdz.statsTracker.stats;

public enum StatType {
	PLAY_TIME,
	KILLS,
	DEATHS,
	KDR,
	HEAD_DROPS,
	KOTH_WINS,
	BLOCKS_MINED,
	BLOCKS_PLACED,
	MOB_KILLS,
	EXP_GAINED,
	DIST_WALKED;
	
	public String toPlainString(){
		switch(this){
		case BLOCKS_MINED: 			return "Blocks mined";
		case BLOCKS_PLACED:			return "Blocks placed";
		case DEATHS:				return "Deaths";
		case DIST_WALKED:			return "Distance walked";
		case EXP_GAINED:			return "Exp gained";
		case HEAD_DROPS:			return "Heads removed";
		case KDR:					return "KDR";
		case KILLS:					return "Kills";
		case KOTH_WINS:				return "KOTH wins";
		case MOB_KILLS:				return "Monster kills";
		case PLAY_TIME:				return "Play time";
		default:					return this.toString();
		}
	}
	
	public String valueToString(double value){
		switch(this){
		case BLOCKS_MINED: 			return (int)value+"";
		case BLOCKS_PLACED:			return (int)value+"";
		case DEATHS:				return (int)value+"";
		case DIST_WALKED:			return distanceFromMeters((int)value);
		case EXP_GAINED:			return (int)value+"";
		case HEAD_DROPS:			return (int)value+"";
		case KDR:					return value+"";
		case KILLS:					return (int)value+"";
		case KOTH_WINS:				return (int)value+"";
		case MOB_KILLS:				return (int)value+"";
		case PLAY_TIME:		return timeFromSeconds((int)value);
		default:					return value+"";
		}
	}
	
	private static String timeFromSeconds(int totalSeconds){
		int days = totalSeconds / 86400;
		int hours = (totalSeconds % 86400 ) / 3600 ;
		int minutes = ((totalSeconds % 86400 ) % 3600 ) / 60 ;
		int seconds = ((totalSeconds % 86400 ) % 3600 ) % 60 ;
		if (days > 0)
			return days+"d "+hours+"h "+minutes+"m "+seconds+"s";
		if (hours > 0)
			return hours+"h "+minutes+"m "+seconds+"s";
		if (minutes > 0)
			return minutes+"m "+seconds+"s";
		return seconds+"s";
	}
	
	private static String distanceFromMeters(int meters){
		double km = meters/1000.0;
		if (km > 0)
			return km+"km";
		return meters+"m";
	}
}
