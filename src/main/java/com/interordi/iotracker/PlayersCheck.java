package com.interordi.iotracker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.interordi.iotracker.structs.RegionTrack;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayersCheck implements Runnable {

	private IOTracker plugin;
	private Map< UUID, PlayerTracking > players;
	private Map< String, RegionTrack > regions;
	
	
	public PlayersCheck(IOTracker ioTracker) {
		this.plugin = ioTracker;
		players = new HashMap< UUID, PlayerTracking >();
	}
	
	
	@Override
	public void run() {
		
		int nbPlayers = this.plugin.getServer().getOnlinePlayers().size();
		if (nbPlayers == 0)
			return;
		
		//Get the list of regions
		//Not storing this locally to get an updated list
		regions = this.plugin.getRegions();
		
		//Get the list of online players
		for (Player oPlayer : Bukkit.getOnlinePlayers()) {
			UUID playerUuid = oPlayer.getUniqueId();
			
			//If the player isn't found...
			if (!this.players.containsKey(playerUuid)) {
				this.plugin.getLogger().info("Player not found!");
				continue;
			}
			
			PlayerTracking tracks = this.players.get(playerUuid);
			Location oldLocation = tracks.getLocation();
			Location newLocation = oPlayer.getLocation();
			String newWorld = oPlayer.getLocation().getWorld().getName();
			
			//If position didn't change...
			if (oldLocation != null &&
				oldLocation.getWorld() == newLocation.getWorld() &&
				oldLocation.getBlockX() == newLocation.getBlockX() &&
				oldLocation.getBlockZ() == newLocation.getBlockZ() &&
				oldLocation.getBlockY() == newLocation.getBlockY())
				continue;
			
			//Update position
			tracks.setLocation(newLocation);
			
			//If the player isn't in a tracked world, exit
			boolean found = false;
			for (String world : plugin.worlds) {
				if (world.equals(newLocation.getWorld().getName())) {
					found = true;
					break;
				}
			}
			if (!found)
				continue;
			
			Set< RegionTrack > regionsActiveBefore = tracks.getRegionsActive();
			Set< RegionTrack > regionsToClear = new HashSet< RegionTrack >(regionsActiveBefore);
			
			//Check regions
			for (Map.Entry< String, RegionTrack > entry : regions.entrySet()) {
				
				String regionName = entry.getKey();
				RegionTrack regionData = entry.getValue();
				Location min = regionData.getMin();
				Location max = regionData.getMax();
				
				//Check if the player is in this region
				if (min.getBlockX() <= newLocation.getBlockX() && newLocation.getBlockX() <= max.getBlockX() &&
					min.getBlockZ() <= newLocation.getBlockZ() && newLocation.getBlockZ() <= max.getBlockZ() &&
					min.getBlockY() <= newLocation.getBlockY() && newLocation.getBlockY() <= max.getBlockY() &&
					newLocation.getWorld().getName().equals(regionData.getWorld())) {
					
					RegionTrack rt = new RegionTrack(regionData.getWorld(), regionData.getName());
					regionsToClear.remove(rt);
					
					//In the region already, ignore
					if (regionsActiveBefore.contains(rt)) {
						continue;
					}
					
					//Not in this region before, now active: add him
					tracks.addRegion(newWorld, regionName);
					tracks.visitRegion(newWorld, regionName);
				}
			}
			
			//Remove the regions where the player is no longer active
			for (RegionTrack rt : regionsToClear) {
				tracks.removeRegion(rt);
			}
			
			
			//Example code
			//.distance() is expensive
			//it's much better to use location.distanceSquared() and compare against the square of the wanted distance
			/*
			double ody = oPlayer.getLocation().distance(dPlayer);
			if (ody <= 15) {
				oPlayer.sendMessage(ChatColor.RED + (ChatColor.ITALIC + "I hear someone nearby..."));
			}
			*/
		}
	}
	
	
	
	public void addPlayer(UUID uuid, Location loc) {
		if (this.players.containsKey(uuid))
			return;
		
		PlayerTracking tracks = new PlayerTracking(uuid, loc);
		this.players.put(uuid, tracks);
	}
	
	
	public void removePlayer(UUID uuid) {
		if (!this.players.containsKey(uuid))
			return;
		
		this.players.remove(uuid);
	}
	
	
	public void visitRegion(UUID uuid, String world, String region, Integer nbVisits) {
		if (!this.players.containsKey(uuid))	return;
		
		PlayerTracking pt = this.players.get(uuid);
		pt.visitRegion(world, region, nbVisits);
	}
	
	
	public void setRegionsActive(UUID uuid, Set< RegionTrack > regions) {
		if (!this.players.containsKey(uuid))	return;
		
		PlayerTracking pt = this.players.get(uuid);
		pt.setRegionsActive(regions);
	}
	
	
	public Map< UUID, PlayerTracking > getPlayers() {
		return this.players;
	}


	//Get the amount of players in each specified region
	public Set< UUID > getPlayersInRegion(RegionTrack region) {

		Set< UUID > inRegion = new HashSet< UUID >();

		for (UUID uuid : players.keySet()) {
			for (RegionTrack regionCheck : players.get(uuid).getRegionsActive()) {
				if (region.equals(regionCheck)) {
					inRegion.add(uuid);
					break;
				}
			}
		}

		return inRegion;
	}
}
