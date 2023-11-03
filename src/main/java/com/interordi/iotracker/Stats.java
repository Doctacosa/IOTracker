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
				for (String world: worlds) {
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
						Integer nbVisits = playerData.getInt(regionName);
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

		Map< UUID, PlayerTracking > players = this.plugin.getPlayers();
		Map< UUID, PlayerTracking > playersCopy = new HashMap< UUID, PlayerTracking >();
		playersCopy.putAll(players);
	
		//Run on its own thread to avoid holding up the server
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			File statsFile = new File(this.statsPath);
			FileConfiguration statsAccess = YamlConfiguration.loadConfiguration(statsFile);

			for (Map.Entry< UUID , PlayerTracking > entry : playersCopy.entrySet()) {
				UUID uuid = entry.getKey();
				PlayerTracking tracking = entry.getValue();
				String world = tracking.getLocation().getWorld().getName();
				
				Map< RegionTrack, Integer > visits = new HashMap< RegionTrack, Integer>();
				visits.putAll(tracking.getVisits());

				for (Map.Entry< RegionTrack , Integer > visitEntry : visits.entrySet()) {
					String regionName = visitEntry.getKey().getName();
					Integer nbVisits = visitEntry.getValue();

					statsAccess.set("visits." + world + "." + uuid + "." + regionName, nbVisits);
				}
				
				Set< RegionTrack > inRegions = tracking.getRegionsActive();
				List< RegionTrack > inRegionsTemp = new ArrayList< RegionTrack >();
				for (RegionTrack region : inRegions) {
					inRegionsTemp.add(region);
				}
				statsAccess.set("regionsactive." + world + "." + uuid, inRegionsTemp);
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
