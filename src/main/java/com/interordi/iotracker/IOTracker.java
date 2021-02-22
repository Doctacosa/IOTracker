package com.interordi.iotracker;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.interordi.iotracker.structs.RegionQuery;
import com.interordi.iotracker.structs.RegionTrack;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public final class IOTracker extends JavaPlugin {
	
	private Regions regionsManager;
	private PlayersCheck playersCheck;
	private Stats stats;

	private String worldGuardPath = "";
	public String[] worlds;
	private List< RegionQuery > regionsQuery;
	

	public void onEnable() {

		regionsQuery = new LinkedList< RegionQuery >();

		//Always ensure we've got a copy of the config in place (does not overwrite existing)
		this.saveDefaultConfig();

		//Get the location of the WorldGuard file
		worldGuardPath = this.getConfig().getString("regions-file");

		if (worldGuardPath == null) {
			getLogger().info("No regions file defined, no checks will be done.");
			return;
		}

		String worldsTemp = this.getConfig().getString("worlds");
		worlds = worldsTemp.split(",");


		ConfigurationSection regionsCS = this.getConfig().getConfigurationSection("regions-query");
		if (regionsCS != null) {
			Set< String > cs = regionsCS.getKeys(false);
			if (cs != null) {
				for (String name : cs) {
					
					String display = regionsCS.getString(name + ".display");
					String warning = regionsCS.getString(name + ".warning");

					regionsQuery.add(new RegionQuery(name, display, warning));
				}
			}
		}


		File source = new File(worldGuardPath);
		if (!source.exists()) {
			getLogger().info("Regions file not found, no checks will be done.");
			return;
		}


		new LoginListener(this);
		
		getLogger().info("IOTracker enabled");
		
		//Get the list of available regions
		this.regionsManager = new Regions(this, worldGuardPath);
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
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		//Get the number of players in each defined in a given region
		if (cmd.getName().equalsIgnoreCase("anyone")) {

			if (!sender.hasPermission("iotracker.anyone")) {
				sender.sendMessage(ChatColor.RED + "You are not allowed to use this command!");
				return true;
			}

			sender.sendMessage(ChatColor.BOLD + "Player activity by area:");
			for (RegionQuery region : regionsQuery) {
				int nbPlayers = this.playersCheck.getPlayersInRegion(region.id).size();
				
				ChatColor status = ChatColor.GRAY;
				if (region.warningType.equalsIgnoreCase("empty")) {
					if (nbPlayers == 0)
						status = ChatColor.RED;
					else
						status = ChatColor.GREEN;

				} else if (region.warningType.equalsIgnoreCase("busy")) {
					if (nbPlayers == 0)
						status = ChatColor.GREEN;
						else
					status = ChatColor.RED;
				}

				String playersLabel;
				if (nbPlayers >= 2)
					playersLabel = nbPlayers + " players";
					else if (nbPlayers == 1)
					playersLabel = nbPlayers + " player";
					else
					playersLabel = "Empty";

				sender.sendMessage(ChatColor.BOLD + region.displayName + ": " + ChatColor.RESET + status + playersLabel);
			}
			return true;
		}

		return false;
	}

		
	public void addPlayer(Player player) {
		final UUID uuid = player.getUniqueId();
		final Location loc = player.getLocation();
		
		playersCheck.addPlayer(uuid, loc);
		stats.loadPlayer(uuid);
	}
	
	
	public void removePlayer(Player player) {
		this.stats.saveStats();
		this.playersCheck.removePlayer(player.getUniqueId());
	}
	
	
	public Map< String, RegionTrack > getRegions() {
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
	 * - obtain the updated list of regions on each load
	 * 		a simple /iotracker reload would also do the trick...
	 * 		also refresh the copy within PlayersCheck
	 */

}
