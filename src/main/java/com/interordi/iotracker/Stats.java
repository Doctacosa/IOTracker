package com.interordi.iotracker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.interordi.iotracker.structs.RegionTrack;

public class Stats implements Runnable {
	
	IOTracker plugin;
	private boolean saving = false;
	private String statsPath = "plugins/IOTracker/stats.yml";
	
	
	Stats(IOTracker plugin) {
		this.plugin = plugin;
	}

	
	@Override
	public void run() {
		saveStats();
	}
	
	
	//NOTE: Currently unused, the data is read player-by-player
	public void loadStats() {
		File statsFile = new File(this.statsPath);

		try {
			if (!statsFile.exists())
				statsFile.createNewFile();
		} catch (IOException e) {
			System.err.println("Failed to load the stats file");
			e.printStackTrace();
			return;
		}

		FileConfiguration statsAccess = YamlConfiguration.loadConfiguration(statsFile);
		
		ConfigurationSection visitsData = statsAccess.getConfigurationSection("visits");
		if (visitsData == null)
			return;	//Nothing yet, exit
		Set< String > players = visitsData.getKeys(false);
		if (players == null)
			return;	//No players found, exit
		
		
		//Loop on each player
		for (String player : players) {
			Set< String > worlds = visitsData.getConfigurationSection(player).getKeys(false);
			if (worlds != null && !worlds.isEmpty()) {
				for (String world : worlds) {
					UUID uuid = UUID.fromString(player);
					ConfigurationSection playerData = visitsData.getConfigurationSection(uuid.toString());
					
					Set< String > rs = playerData.getKeys(false);
					
					//Loop on each visit for this player
					for (String regionName : rs) {
						Integer nbVisits = playerData.getInt(regionName);
						this.plugin.visitRegion(uuid, world, regionName, nbVisits);
					}
				}
			}
		}
	}
	
	
	public void loadPlayer(UUID uuid) {
		//Do in a separate thread, player loading with large stats files is costly
		Bukkit.getServer().getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
			@Override
			public void run() {
				File statsFile = new File(statsPath);
				FileConfiguration statsAccess = YamlConfiguration.loadConfiguration(statsFile);
				
				ConfigurationSection playerData = statsAccess.getConfigurationSection("visits." + uuid);
				if (playerData == null)
					return;	//No player found, exit
				

				Set< String > worlds = playerData.getKeys(false);
							
				for (String world : worlds) {

					ConfigurationSection worldData = playerData.getConfigurationSection(world);
					if (worldData == null)
						return;	//Shouldn't happen, but...

					Set< String > regions = worldData.getKeys(false);

					//Loop on each visit for this player
					for (String regionName : regions) {
						Integer nbVisits = worldData.getInt(regionName);
						plugin.visitRegion(uuid, world, regionName, nbVisits);
					}
				}
				

				List< String > worldsTemp = statsAccess.getStringList("regionsactive." + uuid);
				if (worldsTemp != null) {
					for (String world : worldsTemp) {
						List< String > inRegionsTemp = statsAccess.getStringList("regionsactive." + uuid + "." + world);

						Set< RegionTrack > inRegions = new HashSet< RegionTrack >();
						for (int i = 0; i < inRegionsTemp.size(); i++) {
							RegionTrack rt = new RegionTrack(world, inRegionsTemp.get(i));
							inRegions.add(rt);
						}
						plugin.setRegionsActive(uuid, inRegions);
					}
				}

			}
		});
	}
	
	
	public void saveStats() {
		//No need to save if we're already saving
		if (saving)
			return;

		saving = true;

		//Work off a copy of the list
		Map< UUID, PlayerTracking > players = new HashMap< UUID, PlayerTracking >(this.plugin.getPlayers());
	
		//Run on its own thread to avoid holding up the server
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			File statsFile = new File(this.statsPath);
			FileConfiguration statsAccess = YamlConfiguration.loadConfiguration(statsFile);

			for (Map.Entry< UUID, PlayerTracking > entry : players.entrySet()) {
				UUID uuid = entry.getKey();
				PlayerTracking tracking = entry.getValue();
				
				Map< RegionTrack, Integer > visits = new HashMap< RegionTrack, Integer>();
				visits.putAll(tracking.getVisits());

				for (Map.Entry< RegionTrack, Integer > visitEntry : visits.entrySet()) {
					String world = visitEntry.getKey().getWorld();
					String regionName = visitEntry.getKey().getName();
					Integer nbVisits = visitEntry.getValue();

					statsAccess.set("visits." + uuid + "." + world + "." + regionName, nbVisits);
				}
				
				Set< RegionTrack > inRegions = tracking.getRegionsActive();
				//Restructure in a < world, regions > map
				Map< String, Set< String > > inRegionsTemp = new HashMap< String, Set< String > >();
				for (RegionTrack region : inRegions) {
					if (!inRegionsTemp.containsKey(region.getWorld()))
						inRegionsTemp.put(region.getWorld(), new HashSet< String >());
					Set< String > regions = inRegionsTemp.get(region.getWorld());
					regions.add(region.getName());
					inRegionsTemp.put(region.getWorld(), regions);
				}
				//Save
				for (Map.Entry< String, Set< String > > activeEntry : inRegionsTemp.entrySet()) {
					statsAccess.set("regionsactive." + uuid + "." + activeEntry.getKey(), activeEntry.getValue().toArray());
				}
			}
			
			try {
				statsAccess.save(statsFile);
			} catch (IOException e) {
				e.printStackTrace();
			}

			saving = false;
		});
	}
}
