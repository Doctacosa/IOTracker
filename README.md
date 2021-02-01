# IOTracker

This Bukkit plugin tracks the movement of Minecraft players through all defined WorldGuard regions.

All records go in a `stats.yml` file, which records the number of times that each player has entered each defined region. The regions a player is present in is saved through reconnections and server restarts.

This plugin doesn't have a direct dependency on WorldGuard: it reads the regions files created by the plugin directly from the filesystem; no API features are used.
