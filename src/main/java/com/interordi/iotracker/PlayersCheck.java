package com.interordi.iotracker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
			
			//this.plugin.getLogger().info("Check player " + playerName);
			
			//If the player isn't found...
			if (!this.players.containsKey(playerUuid)) {
				this.plugin.getLogger().info("Player not found!");
				continue;
			}
			
			PlayerTracking tracks = this.players.get(playerUuid);
			Location oldLocation = tracks.getLocation();
			Location newLocation = oPlayer.getLocation();
			
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
			
			Set< String > regionsActiveBefore = tracks.getRegionsActive();
			Set< String > regionsToClear = new HashSet< String >(regionsActiveBefore);
			
			//this.plugin.getLogger().info("Nb regions: " + regions.size());
			
			//Check regions
			for (Map.Entry< String, RegionTrack > entry : regions.entrySet()) {
				
				String regionName = entry.getKey();
				RegionTrack regionData = entry.getValue();
				Location min = regionData.getMin();
				Location max = regionData.getMax();
				
				//Check if the player is in this region
				if (min.getBlockX() <= newLocation.getBlockX() && newLocation.getBlockX() <= max.getBlockX() &&
					min.getBlockZ() <= newLocation.getBlockZ() && newLocation.getBlockZ() <= max.getBlockZ() &&
					min.getBlockY() <= newLocation.getBlockY() && newLocation.getBlockY() <= max.getBlockY()) {
					
					regionsToClear.remove(regionName);
					
					//In the region already, ignore
					if (regionsActiveBefore.contains(regionName)) {
						//this.plugin.getLogger().info(playerName + " still in region " + regionName);
						continue;
					}
					
					//Not in this region before, now active: add him
					//this.plugin.getLogger().info(playerName + " entering region " + regionName);
					tracks.addRegion(regionName);
					tracks.visitRegion(regionName);
				}
			}
			
			//Remove the regions where the player is no longer active
			Iterator< String > it = regionsToClear.iterator();
			while (it.hasNext()) {
				String regionName = it.next();
				//this.plugin.getLogger().info(playerName + " leaving region " + regionName);
				tracks.removeRegion(regionName);
			}
			
			
			//Example code
			//.distance() is expensive
			//it's much better to use location.distanceSquared() and compare against the square of the wanted distance
			//double ody = oPlayer.getLocation().distance(dPlayer);
			//if(ody <= 15){
			//oPlayer.sendMessage(ChatColor.RED + (ChatColor.ITALIC + "I hear someone nearby..."));
			//}
		}
	}
	
	
	
	public void addPlayer(Player player) {
		UUID uuid = player.getUniqueId();
		if (this.players.containsKey(player.getUniqueId()))	return;
		
		PlayerTracking tracks = new PlayerTracking(uuid, player.getLocation());
		this.players.put(uuid, tracks);
	}
	
	
	public void removePlayer(Player player) {
		UUID uuid = player.getUniqueId();
		if (!this.players.containsKey(uuid))	return;
		
		//TODO: Save player's data?
		this.players.remove(uuid);
	}
	
	
	public void visitRegion(UUID uuid, String region, Integer nbVisits) {
		if (!this.players.containsKey(uuid))	return;
		
		PlayerTracking pt = this.players.get(uuid);
		pt.visitRegion(region, nbVisits);
	}
	
	
	public void setRegionsActive(UUID uuid, Set< String > regions) {
		if (!this.players.containsKey(uuid))	return;
		
		PlayerTracking pt = this.players.get(uuid);
		pt.setRegionsActive(regions);
	}
	
	
	public Map< UUID, PlayerTracking > getPlayers() {
		return this.players;
	}


	//Get the amount of players in each specified region
	public Set< UUID > getPlayersInRegion(String region) {

		Set< UUID > inRegion = new HashSet< UUID >();

		for (UUID uuid : players.keySet()) {
			for (String regionCheck : players.get(uuid).getRegionsActive()) {
				if (region.equalsIgnoreCase(regionCheck)) {
					inRegion.add(uuid);
					break;
				}
			}
		}


		return inRegion;
	}
}
