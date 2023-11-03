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


	//The equality functions ignore coordinates on purpose
	//Only the world and region names are relevant for matching
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof RegionTrack))
			return false;

		RegionTrack other = (RegionTrack)o;
		return (
			other.getWorld().equals(this.getWorld()) &&
			other.getName().equals(this.getName())
		);
	}
	
	@Override
	public int hashCode() {
		return (this.getWorld() + this.getName()).hashCode();
	}
}
