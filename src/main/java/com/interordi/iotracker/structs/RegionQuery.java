package com.interordi.iotracker.structs;

public class RegionQuery {

	public String id;
	public String world;
	public String displayName;
	public String warningType;


	public RegionQuery(String id, String world, String displayName, String warningType) {
		this.id = id;
		this.world = world;
		this.displayName = displayName;
		this.warningType = warningType;
	}
	
}
