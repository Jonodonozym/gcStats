
package jdz.statsTracker.commandHandlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jdz.statsTracker.achievement.AchievementInventories;
import jdz.statsTracker.achievement.AchievementShop;
import jdz.statsTracker.main.Config;
import jdz.statsTracker.stats.PlayTimeRecorder;
import jdz.statsTracker.util.SqlApi;

public class AchievementCommands  implements CommandExecutor{
	static String[] gcaHelpMessages = new String[]{
			ChatColor.GRAY+"=============[ "+ChatColor.GOLD+"GCA Help"+ChatColor.GRAY+" ]=============",
			ChatColor.WHITE+"/gcs help - commands for stats",
			ChatColor.WHITE+"/gca - shows your cross-server achievements",
			ChatColor.WHITE+"/gca [player] - shows another player's cross-server achievements",
			ChatColor.WHITE+"/gca points [server] - shows the amount of points you have racked up",
			ChatColor.WHITE+"/gca redeem - redeem your achievement points for shiny new items!",
			ChatColor.WHITE+"/gca about - info about the plugin",
			ChatColor.GRAY+"===================================="
			
	};
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (cmd.getName().equals(Config.achCommand)){
			if (!(sender instanceof Player)){
				sender.sendMessage("You must be a player to run this command");
				return true;
			}
				
			Player player = (Player)sender;
			
			if(args.length == 0){
				if (SqlApi.isConnected()){
					PlayTimeRecorder.updateTime(player);
					AchievementInventories.openServerSelect(player, player);
				}
				else
					player.sendMessage(ChatColor.RED+"Couldn't connect to the stats and achievements database D:");
			}

			else switch(args[0].toLowerCase()){
				case "about": sender.sendMessage(StatsCommands.aboutMessages); break;
				case "help": sender.sendMessage(gcaHelpMessages); break;
				case "redeem":
				case "shop":
					if (SqlApi.isConnected())
						AchievementShop.openShop(player);
					else
						player.sendMessage(ChatColor.RED+"Couldn't connect to the stats and achievements database D:");
					break;
				case "bal":
				case "balance":
				case "points": 
					if (SqlApi.isConnected()){
						if (args.length == 1)
							sender.sendMessage(ChatColor.GREEN+"Achievement Points: "+ChatColor.YELLOW+SqlApi.getAchievementPoints(player));
						else if (Config.servers.contains(args[1].replaceAll("_", " ")))
							sender.sendMessage(ChatColor.GREEN+"Achievement Points: "+ChatColor.YELLOW+SqlApi.getAchievementPoints(player, args[1]));
						else
							sender.sendMessage(ChatColor.RED+"'"+args[1].replaceAll("_", " ")+"' is not a valid server!");
					}
					else
						player.sendMessage(ChatColor.RED+"Couldn't connect to the stats and achievements database D:");
					break;
				default:
					if (SqlApi.isConnected()){
						@SuppressWarnings("deprecation")
						OfflinePlayer otherPlayer = Bukkit.getOfflinePlayer(args[0]);
						if(SqlApi.hasPlayer(Config.serverName, otherPlayer)){
							if (otherPlayer.isOnline())
								PlayTimeRecorder.updateTime((Player)otherPlayer);
							AchievementInventories.openServerSelect(player, otherPlayer);
						}
						else
							sender.sendMessage(ChatColor.RED+"'"+args[0]+"' is not a valid player");
					}
					else
						player.sendMessage(ChatColor.RED+"Couldn't connect to the stats and achievements database D:");
					break;
			}
			
			return true;
			}
		return false;
	}
}
