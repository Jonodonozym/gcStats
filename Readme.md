# Stats and Achievements tracker
This is a plugin made for GuildCraft designed to track player statistics in game on an Sql database.
Its secondary feature is to track player achievements based on those statistics, rewarding players with achievement points that they can redeem in a shop GUI.

## Setup
To set up the plugin, run it once and then enter the SQL database's url and information in the generated
config.yml file under the gcStats folder. I suggest you also look at the other parts of the config file to see what you can tweak.

You can also configure achievements by adding them to the achievements.yml file. There are already a few examples there for you.

Lastly, you can configure the achievement shop using the AchievementShop.yml file. You can follow the format given along with the examples to add your own items to the shop.

In-game, you can now type /gcs help or /gca help for help with the stats or achievements respectivley

## How to extend Statistics
If you want to add a new statistic type (e.g. a custom plugin's stat) and the creator, Jonodonozym, isn't around to help, do the following steps:

1) add the stat type's name to the StatType enumerator (jdz.statsTracker.stats.StatType)
2) Either add event listeners which increase the stat using SqlApi.addStat() when an event happens, or create a new TimedTask that sets the player stat based on something else in code, such as another plugin's data, using SqlApi.setStat().

That's it! you're done! The new statistic type is added. Now you can add achievements using this new stat type and update the plugin on your server. You don't even have to update the plugins on all servers, just the ones which track the new statistic type.
