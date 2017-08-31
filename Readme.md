#Stats and Achievements tracker
This is a plugin made for GuildCraft designed to track player statistics in game on an Sql database.
Its secondary feature is to track player achievements based on those statistics.

##Setup
To set up the plugin, run it once and then enter the SQL database's url and information in the generated
config.yml file under the gcStats folder. I suggest you also look at the other parts of the config file to see what you can tweak.

You can also configure achievements by adding them to the achievements.yml file. One has already been put in
as an example for you.

In-game, you can now type /gcs help or /gca help for help with the stats or achievements respectivley

##Development
If you want to add a new statistic type and the creator isn't around to help, do the following steps:

1) add the stat type's name to the StatType enumerator (jdz.statsTracker.stats.StatType)
2) Either add event listeners which increase the stat using SqlApi.addStat() when an event happens, or create a new TimedTask that sets the player stat based on something else in game, such as another plugin's data, using SqlApi.setStat().

That's it! you're done! The new statistic type is added. Now you can add achievements using this new stat type and uptade the plugin on your sever. You don't even have to update the plugins on all servers, just the ones which track the new statistic type.