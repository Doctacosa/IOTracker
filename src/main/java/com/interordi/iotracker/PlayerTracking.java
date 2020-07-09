package com.interordi.iotracker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;

public class PlayerTracking {
	
	private UUID uuid;
	private Location location;
	private Set< String > inRegions;
	private Map< String, Integer > visits;
	
	
	PlayerTracking(UUID uuid, Location location) {
		this.uuid = uuid;
		this.location = location;
		this.inRegions = new HashSet< String >();
		this.visits = new HashMap< String, Integer>();
	}
	
	
	public UUID getUuid() {
		return this.uuid;
	}
	
	
	public Location getLocation() {
		return this.location;
	}
	
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	
	public Set< String > getRegionsActive() {
		return this.inRegions;
	}
	
	
	public Map< String, Integer > getVisits() {
		return this.visits;
	}
	
	
	public void addRegion(String region) {
		inRegions.add(region);
	}
	
	
	public void removeRegion(String region) {
		inRegions.remove(region);
	}
	
	
	public void visitRegion(String region) {
		visitRegion(region, 1);
	}
	
	
	public void visitRegion(String region, Integer count) {
		if (this.visits.containsKey(region)) {
			Integer regionCount = this.visits.get(region);
			this.visits.put(region, regionCount + count);
		} else {
			this.visits.put(region, count);
		}
	}
	
	
	public void setRegionsActive(Set< String > regionsActive) {
		this.inRegions = regionsActive;
	}
}
