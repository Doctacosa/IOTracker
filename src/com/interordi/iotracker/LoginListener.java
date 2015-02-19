package com.interordi.iotracker;

import org.bukkit.event.player.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class LoginListener implements Listener {
	
	private IOTracker plugin;
	
	public LoginListener(IOTracker plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	
	@EventHandler	//Annotation - EventPriority.NORMAL by default
	public void onPlayerLogin(PlayerJoinEvent event) {
		//event.getPlayer().sendMessage("Hi " + event.getPlayer().getDisplayName() + "!");
		//plugin.getLogger().info(event.getPlayer().getDisplayName() + " logged in");
		this.plugin.addPlayer(event.getPlayer());
	}
	
	
	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent event) {
		this.plugin.removePlayer(event.getPlayer());
	}
}
