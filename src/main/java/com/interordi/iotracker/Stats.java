package com.interordi.iotracker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

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
		Set< String > cs = visitsData.getKeys(false);
		if (cs == null)
			return;	//No players found, exit
		
		
		//Loop on each player
		for (String temp : cs) {
			UUID uuid = UUID.fromString(temp);
			ConfigurationSection playerData = visitsData.getConfigurationSection(uuid.toString());
			
			Set< String > rs = playerData.getKeys(false);
			
			//Loop on each visit for this player
			for (String regionName : rs) {
				Integer nbVisits = playerData.getInt(regionName);
				this.plugin.visitRegion(uuid, regionName, nbVisits);
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
				
				Set< String > rs = playerData.getKeys(false);
				
				//Loop on each visit for this player
				for (String regionName : rs) {
					Integer nbVisits = playerData.getInt(regionName);
					plugin.visitRegion(uuid, regionName, nbVisits);
				}
				
				List< String > inRegionsTemp = statsAccess.getStringList("regionsactive." + uuid);
				
				if (inRegionsTemp != null) {
					Set< String > inRegions = new HashSet< String >();
					for (int i = 0; i < inRegionsTemp.size(); i++) {
						inRegions.add(inRegionsTemp.get(i));
					}
					plugin.setRegionsActive(uuid, inRegions);
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
		
		//Run on its own thread to avoid holding up the server
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			File statsFile = new File(this.statsPath);
			FileConfiguration statsAccess = YamlConfiguration.loadConfiguration(statsFile);
			
			for (Map.Entry< UUID , PlayerTracking > entry : players.entrySet()) {
				UUID uuid = entry.getKey();
				PlayerTracking tracking = entry.getValue();
				
				Map< String, Integer > visits = tracking.getVisits();
				
				for (Map.Entry< String , Integer > visitEntry : visits.entrySet()) {
					String regionName = visitEntry.getKey();
					Integer nbVisits = visitEntry.getValue();
					
					statsAccess.set("visits." + uuid + "." + regionName, nbVisits);
				}
				
				Set< String > inRegions = tracking.getRegionsActive();
				List< String > inRegionsTemp = new ArrayList< String >();
				for (String region : inRegions) {
					inRegionsTemp.add(region);
				}
				statsAccess.set("regionsactive." + uuid, inRegionsTemp);
			}
			
			try {
				statsAccess.save(statsFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			saving = false;
		});
	}
}
