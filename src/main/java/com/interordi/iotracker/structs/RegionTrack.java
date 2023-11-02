package com.interordi.iotracker.structs;

import org.bukkit.Location;

public class RegionTrack {

	private String name = null;
	private String world = null;
	private Location min = null;
	private Location max = null;
	
	
	public RegionTrack(String world, String name) {
		this.world = world;
		this.name = name;
	}
	
	
	public RegionTrack(String world, String name, Location min, Location max) {
		this.world = world;
		this.name = name;
		this.min = min;
		this.max = max;
	}
	
	
	public String getWorld() {
		return this.world;
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
