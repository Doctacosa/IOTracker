package com.interordi.iotracker.structs;

public class RegionQuery {

	public String world;
	public String region;
	public String displayName;
	public String warningType = "none";


	public RegionQuery(String world, String region, String displayName, String warningType) {
		this.world = world;
		this.region = region;
		this.displayName = displayName;
		this.warningType = warningType;
	}
	
}
