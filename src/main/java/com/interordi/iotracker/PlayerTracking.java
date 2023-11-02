package com.interordi.iotracker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;

import com.interordi.iotracker.structs.RegionTrack;

public class PlayerTracking {
	
	private UUID uuid;
	private Location location;
	private Set< RegionTrack > inRegions;
	private Map< RegionTrack, Integer > visits;
	
	
	PlayerTracking(UUID uuid, Location location) {
		this.uuid = uuid;
		this.location = location;
		this.inRegions = new HashSet< RegionTrack >();
		this.visits = new HashMap< RegionTrack, Integer>();
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
	
	
	public Set< RegionTrack > getRegionsActive() {
		return this.inRegions;
	}
	
	
	public Map< RegionTrack, Integer > getVisits() {
		return this.visits;
	}
	
	
	public void addRegion(RegionTrack region) {
		inRegions.add(region);
	}
	
	
	public void addRegion(String world, String region) {
		RegionTrack rt = new RegionTrack(world, region);
		inRegions.add(rt);
	}
	
	
	public void removeRegion(String world, String region) {
		RegionTrack rt = new RegionTrack(world, region);
		inRegions.remove(rt);
	}
	
	
	public void removeRegion(RegionTrack rt) {
		inRegions.remove(rt);
	}
	
	
	public void visitRegion(String world, String region) {
		visitRegion(world, region, 1);
	}
	
	
	public void visitRegion(String world, String region, Integer count) {
		RegionTrack rt = new RegionTrack(world, region);
		if (this.visits.containsKey(rt)) {
			Integer regionCount = this.visits.get(rt);
			this.visits.put(rt, regionCount + count);
		} else {
			this.visits.put(rt, count);
		}
	}
	
	
	public void setRegionsActive(Set< RegionTrack > regionsActive) {
		this.inRegions = regionsActive;
	}
}
