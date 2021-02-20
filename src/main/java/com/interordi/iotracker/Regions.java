package com.interordi.iotracker;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.interordi.iotracker.structs.RegionTrack;

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
	private String worldGuardPath = "plugins/WorldGuard/worlds/world/regions.yml";
	
    private FileConfiguration customConfig = null;
    private File regionsFile = null;
    
    //List of available regions
    Map< String, RegionTrack > regions;
    

	public Regions(IOTracker plugin, String worldGuardPath) {
		this.plugin = plugin;
		this.worldGuardPath = worldGuardPath;
	}
	
	
	@Override
	public void run() {
		readAll();
	}
	
	
	//Get the list of regions from the config file
	public void readAll() {
		if (regionsFile == null) {
			regionsFile = new File(this.worldGuardPath);
		}
		this.customConfig = YamlConfiguration.loadConfiguration(regionsFile);
		
		ConfigurationSection regionsConfig = this.customConfig.getConfigurationSection("regions");
		if (regionsConfig == null)
			return;	//No regions found, exit
		
		regions = new HashMap< String, RegionTrack >();
		
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
			double x1, y1, z1, x2, y2, z2;
			
			//Cuboid selection
			if (regionsConfig.getString(name + ".type").equals("cuboid")) {
				x1 = regionsConfig.getDouble(name + ".min.x");
				y1 = regionsConfig.getDouble(name + ".min.y");
				z1 = regionsConfig.getDouble(name + ".min.z");
				x2 = regionsConfig.getDouble(name + ".max.x");
				y2 = regionsConfig.getDouble(name + ".max.y");
				z2 = regionsConfig.getDouble(name + ".max.z");

			} else if (regionsConfig.getString(name + ".type").equals("poly2d")) {
				y1 = regionsConfig.getDouble(name + ".min-y");
				y2 = regionsConfig.getDouble(name + ".max-y");
				x1 = Integer.MAX_VALUE;
				x2 = Integer.MIN_VALUE;
				z1 = Integer.MAX_VALUE;
				z2 = Integer.MIN_VALUE;

				String pointsRaw = regionsConfig.getString(name + ".points");
				Pattern r = Pattern.compile("\\{(.*?)\\}");
				Matcher m = r.matcher(pointsRaw);

				while (m.find()) {
					String point = pointsRaw.substring(m.start(), m.end());

					/*
					//Regex doesn't work, don't know why, screw that thing
					Pattern r2 = Pattern.compile("x=([\\-0-9]*), z=([\\-0-9]*)");
					Matcher m2 = r2.matcher(point);
					m2.matches();

					xp = Integer.parseInt(m2.group(0));
					zp = Integer.parseInt(m2.group(1));
					*/

					int xp = Integer.parseInt(point.substring(point.indexOf("x=") + 2, point.indexOf(", ")));
					int zp = Integer.parseInt(point.substring(point.indexOf("z=") + 2, point.indexOf("}")));

					if (xp < x1)
						x1 = xp;
					if (xp > x2)
						x2 = xp;
					if (zp < z1)
						z1 = zp;
					if (zp > z2)
						z2 = zp;
				}

			} else {
				continue;
			}
			
			//Add this region to the list of available ones
			this.regions.put(
				name,
				new RegionTrack(
					name,
					new Location(null, x1, y1, z1),
					new Location(null, x2, y2, z2)
				)
			);

			//Debug output
			//System.out.println(name + ": " + x1 + "," + y1 + "," + z1 + " --- " + x2 + "," + y2 + "," + z2);
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
	
	
	public Map< String, RegionTrack > getRegions() {
		return this.regions;
	}
}
