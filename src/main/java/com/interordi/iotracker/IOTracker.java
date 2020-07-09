package com.interordi.iotracker;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class IOTracker extends JavaPlugin {
	
	private Regions regionsManager;
	private PlayersCheck playersCheck;
	private Stats stats;
	

	public void onEnable() {
		new LoginListener(this);
		//new ChatListener(this);
		
		getLogger().info("IOTracker enabled");
		
		//Get the list of available regions
		this.regionsManager = new Regions(this);
		this.regionsManager.readAll();
		
		this.playersCheck = new PlayersCheck(this);
		this.stats = new Stats(this);
		
		
		//Read the current stats
		this.stats.loadStats();
		
		//Once the server is running, check for player positions every second
		getServer().getScheduler().scheduleSyncRepeatingTask(this, playersCheck, 5*20L, 1*20L);
		
		//Save the config every minute
		getServer().getScheduler().scheduleSyncRepeatingTask(this, stats, 60*20L, 60*20L);
		
		//Update the list of regions every hour
		//FIXME: Regions don't actually get updated
		getServer().getScheduler().scheduleSyncRepeatingTask(this, regionsManager, 30*60*20L, 60*60*20L);
	}
	
	
	public void onDisable() {
		getLogger().info("IOTracker disabled");
	}
	
	
	public void addPlayer(Player player) {
		this.playersCheck.addPlayer(player);
		this.stats.loadPlayer(player.getUniqueId());
	}
	
	
	public void removePlayer(Player player) {
		this.stats.saveStats();
		this.playersCheck.removePlayer(player);
	}
	
	
	public Map< String, Region > getRegions() {
		return this.regionsManager.getRegions();
	}
	
	
	public void visitRegion(UUID uuid, String region, Integer nbVisits) {
		this.playersCheck.visitRegion(uuid, region, nbVisits);
	}
	
	
	public void setRegionsActive(UUID uuid, Set< String > regions) {
		this.playersCheck.setRegionsActive(uuid, regions);
	}
	
	
	public Map< UUID, PlayerTracking > getPlayers() {
		return this.playersCheck.getPlayers();
	}
	
	
	/*
	 * TODO
	 * - obtenir la liste à jour des regions à chaque changement
	 * 		un simple /iotracker reload ferait aussi l'affaire...
	 * 		faire un refresh de la copie de PlayersCheck aussi
	 */

}
