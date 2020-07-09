package com.interordi.iotracker;

import org.bukkit.Location;

public class Region {

	String name;
	Location min;
	Location max;
	
	
	Region(String name, Location min, Location max) {
		this.name = name;
		this.min = min;
		this.max = max;
	}
	
	
	String getName() {
		return this.name;
	}
	
	
	Location getMin() {
		return this.min;
	}
	
	
	Location getMax() {
		return this.max;
	}
}
