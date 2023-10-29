# IOTracker

This Bukkit plugin tracks the movement of Minecraft players through all defined WorldGuard regions.

All records go in a `stats.yml` file, which records the number of times that each player has entered each defined region. The regions a player is present in is saved through reconnections and server restarts.

This plugin doesn't have a direct dependency on WorldGuard: it reads the regions files created by the plugin directly from the filesystem; no API features are used.


## Configuration

`regions-file`: The location of the WorldGuard regions.yml file to use, relative to the server's location  
`worlds`: The worlds to check for activity; comma-separated list.  
`regions-query`: Which regions should be included in the `/anyone` command. The format goes as such:
```
  region-id:
    display: Display name
    warning: [none|empty|busy]
```

## Commands

`/anyone`: Check the amount of players present in each defined region.


## Permissions

`iotracker.anyone`: Usage of the /anyone command
