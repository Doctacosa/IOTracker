package com.interordi.iotracker.structs;

import org.bukkit.Location;

public class RegionTrack {

	String name;
	Location min;
	Location max;
	
	
	public RegionTrack(String name, Location min, Location max) {
		this.name = name;
		this.min = min;
		this.max = max;
	}
	
	
	public String getName() {
		return this.name;
	}
	
	
	public Location getMin() {
		return this.min;
	}
	
	
	public Location getMax() {
		return this.max;
	}
}
