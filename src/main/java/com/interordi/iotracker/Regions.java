package com.interordi.iotracker;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Regions implements Runnable {

	/*
	//TODO: Catch the creation of new regions
	 		Maybe using a check on the file used by WorldGuard
	*/
	
	private IOTracker plugin;
	private String worldGuardPath = "plugins/WorldGuard/worlds/world/";
	
    private FileConfiguration customConfig = null;
    private File regionsFile = null;
    
    //List of available regions
    Map< String, Region > regions;
    

	public Regions(IOTracker plugin) {
		this.plugin = plugin;
	}
	
	
	@Override
	public void run() {
		readAll();
	}
	
	
	//Get the list of regions from the config file
	public void readAll() {
		if (regionsFile == null) {
			regionsFile = new File(this.worldGuardPath + "regions.yml");
		}
		this.customConfig = YamlConfiguration.loadConfiguration(regionsFile);
		
		ConfigurationSection regionsConfig = this.customConfig.getConfigurationSection("regions");
		if (regionsConfig == null)
			return;	//No regions found, exit
		
		regions = new HashMap< String, Region >();
		
		/*
		Map<String, Object> regions = regionsConfig.getValues(false);
		if (regions == null)
			return;	//No regions found, exit
		
		//Iterate on each region found
		for (Map.Entry< String, Object > entry : regions.entrySet()) {
			entry.getKey();
			entry.getValue();
		}
		*/
		
		Set< String > cs = regionsConfig.getKeys(false);
		if (cs == null)
			return;	//No regions found, exit
		
		
		//Clear the current list of regions
		this.regions.clear();
		
		
		for (String name : cs) {
			//Check against missing data
			String maxStr = regionsConfig.getString(name + ".max");
			if (maxStr == null || maxStr.isEmpty())
				continue;
			
			//Store this region
			double x1 = regionsConfig.getDouble(name + ".min.x");
			double y1 = regionsConfig.getDouble(name + ".min.y");
			double z1 = regionsConfig.getDouble(name + ".min.z");
			double x2 = regionsConfig.getDouble(name + ".max.x");
			double y2 = regionsConfig.getDouble(name + ".max.y");
			double z2 = regionsConfig.getDouble(name + ".max.z");
			
			//Add this region to the list of available ones
			this.regions.put(
				name,
				new Region(
					name,
					new Location(null, x1, y1, z1),
					new Location(null, x2, y2, z2)
				)
			);
			
			//Debug output
			//System.out.println(min.getBlockX() + "," + min.getBlockY() + "," + min.getBlockZ() + " --- " + max.getBlockX() + "," + max.getBlockY() + "," + max.getBlockZ());
		}
	}
	
	
	public FileConfiguration getCustomConfig() {
		if (this.customConfig == null) {
			this.readAll();
		}
		return this.customConfig;
	}
	
	
	//Ugly method to get a location out of a YAML config string  
	@SuppressWarnings("unused")
	private Location stringToLocation(String str) {
		
		Location l = new Location(null, 0.0, 0.0, 0.0);
		
		int pos1 = str.indexOf("x: ", 0);
		int pos2 = str.indexOf(",", pos1);
		
		this.plugin.getLogger().info(str);
		this.plugin.getLogger().info(str.substring(pos1 + 3, pos2));
		
		l.setX(Double.parseDouble(str.substring(pos1 + 3, pos2)));
		
		pos1 = str.indexOf("y: ", 0);
		pos2 = str.indexOf(",", pos1);
		l.setY(Double.parseDouble(str.substring(pos1 + 3, pos2)));
		
		pos1 = str.indexOf("z: ", 0);
		pos2 = str.indexOf(",", pos1);
		l.setZ(Double.parseDouble(str.substring(pos1 + 3, pos2)));
		
		return l;
	}
	
	
	public Map< String, Region > getRegions() {
		return this.regions;
	}
}
