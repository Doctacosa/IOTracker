# IOTracker

This Bukkit plugin tracks the movement of Minecraft players through all defined WorldGuard regions.

All records go in a `stats.yml` file, which records the number of times that each player has entered each defined region. The regions a player is present in is saved through reconnections and server restarts.

This plugin doesn't have a direct dependency on WorldGuard: it reads the regions files created by the plugin directly from the filesystem. No API features are used.

Only cuboid regions (the default) are used as-is. Polys will be extended to the largest bounding box (a rectangle), then that area will be used for the detection.


### Logging player activity

The first, main use of this plugin is to track how many times a player has entered a given region. This can be used to monitor player activity, grant rewards for entering a specific zone, or whatever else you see fit.

### /anyone command

The `/anyone` command can be used by any player with the permission to check if *anyone* is within a region at the given moment. Here's a sample configuration:
```
regions-query:
  world_test:
    spawn:
      display: Spawn point
      warning: none
    beds:
      display: Sleeping quarters
      warning: busy
    lookout:
      display: Watch tower
      warning: empty
```

A player using `/anyone` will get a report on the status of each defined region.
* The region named "spawn" will simply display its name and the number of players there, if any. 
* The one named "beds" will be highlighted in green if empty, meaning that it's available; and red if currently occupied.
* The one named "lookout" is the opposite: red if empty (no one is keeping watch), while green if someone is in position.

By default, the command will check for players active in the regions of the server running the plugin. You can instead configure it to check for another server accessible on the same file system with the parameters `worldguard-check` (where the regions are defined) and `stats-check` (the file with the recorded visits). For example, your config file could look like this:
```
worldguard-path: plugins/WorldGuard/
worldguard-check: ../server-other/plugins/WorldGuard/
stats-check: ../server-other/plugins/IOTracker/stats.yml
worlds: world_local,world_local_nether,world_local_the_end
regions-query:
  world_other:
    sora-monument:
      display: Halls of Nyx
      warning: busy
```


## Configuration

`worldguard-path`: The path to WorldGuard, if not the standard. Defaults to `plugins/WorldGuard/`.  
`worldguard-check`: The path to WorldGuard of another server, if wanted. Defauls to the value of `worldguard-path`.  
`stats-check`: Optional. The file to examine for the `/anyone` command, if not the one for this server.  
`worlds`: The worlds to monitor for activity; comma-separated list.  
`regions-query`: Which regions should be included in the `/anyone` command. Explained above.  


## Commands

`/anyone`: Check the amount of players present in each defined region.


## Permissions

`iotracker.anyone`: Usage of the /anyone command
