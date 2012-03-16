package com.realmdata.metrics;

import java.util.logging.Level;
import java.text.MessageFormat;

import java.util.Map;
import java.util.HashMap;

import org.json.simple.JSONObject;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.player.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.world.*;
import org.bukkit.event.weather.*;
import org.bukkit.event.server.*;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Creature;


public class Plugin extends JavaPlugin implements Listener {
    protected final Events events = new Events("iron.minecarts.com");
    
    @Override
    public void onEnable() {
        // start sessions for online players
        for(Player player : getServer().getOnlinePlayers()) {
            Sessions.getSession(player, true);
        }
        
        getServer().getPluginManager().registerEvents(Sessions.getInstance(), this);
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void serverPing(final ServerListPingEvent event) {
        events.track(new Event("Server ping", "Server", "Ping").setUser(null, null, event.getAddress().getHostAddress()));
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void playerLogin(final PlayerLoginEvent event) {
        events.track(new Event("Player login", "Player", "Login", event.getHostname(), event.getResult().toString()).setUser(event.getPlayer()));
    }
    
    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        events.track(new Event("Player join", "Player", "Join").setUser(event.getPlayer()).setPlace(event.getPlayer().getLocation()));
    }
    
    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        events.track(new Event("Player quit", "Player", "Quit").setUser(event.getPlayer()).setPlace(event.getPlayer().getLocation()));
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void blockBreak(final BlockBreakEvent event) {
        switch(event.getPlayer().getGameMode()) {
            case CREATIVE:
                return;
        }
        
        switch(event.getBlock().getType()) {
            case BEDROCK:
            case SPONGE:
            case DRAGON_EGG:
            case REDSTONE_ORE:
            case GOLD_ORE:
            case IRON_ORE:
            case LAPIS_ORE:
            case DIAMOND_ORE:
            case OBSIDIAN:
            case MOSSY_COBBLESTONE:
            case MOB_SPAWNER:
                events.track(new Event("Player break block", "Player", "Block", event.getBlock().getType().toString()).setUser(event.getPlayer()).setPlace(event.getBlock().getLocation()));
                break;
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void creatureSpawn(final CreatureSpawnEvent event) {
        events.track(new Event("Creature spawn", "Creature", "Spawn", event.getEntity().getType().toString(), event.getSpawnReason().toString()).setPlace(event.getLocation()));
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void entityDeath(final EntityDeathEvent event) {
        Entity entity = event.getEntity();
        EntityDamageEvent lastDamage = entity.getLastDamageCause();
        if(event.getEntity() instanceof Creature) {
            events.track(new Event("Creature death", "Creature", "Death", entity.getType().toString(), lastDamage == null ? "Unknown cause" : lastDamage.getCause().toString()).setPlace(entity.getLocation()));
        }
        else if(event.getEntity() instanceof Player) {
            events.track(new Event("Player death", "Player", "Death", lastDamage == null ? "Unknown cause" : lastDamage.getCause().toString()).setUser((Player) entity).setPlace(entity.getLocation()));
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void entityExplode(final EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        switch(event.getEntity().getType()) {
            case CREEPER:
                events.track(new Event("Creeper explode", "Creature", "Explode", entity.getType().toString()).setPlace(entity.getLocation()));
                break;
            case PRIMED_TNT:
                events.track(new Event("TNT explode", "Block", "Explode", entity.getType().toString()).setPlace(entity.getLocation()));
                break;
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void lightningStrike(final LightningStrikeEvent event) {
        events.track(new Event("Lightning strike", "World", "Weather").setPlace(event.getLightning().getLocation()));
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void structureGrow(final StructureGrowEvent event) {
        Player player = event.getPlayer();
        if(player != null) {
            events.track(new Event("Player grow tree", "Player", "Grow", "Structure", "Tree", event.getSpecies().toString()).setUser(player).setPlace(event.getLocation()));
        }
        else {
            events.track(new Event("Tree grow", "World", "Grow", "Structure", "Tree", event.getSpecies().toString()).setPlace(event.getLocation()));
        }
    }
    
    
    public void log(String message) {
        log(Level.INFO, message);
    }
    public void log(Level level, String message) {
        getLogger().log(level, message);
    }
    public void log(String message, Object... args) {
        log(MessageFormat.format(message, args));
    }
    public void log(Level level, String message, Object... args) {
        log(level, MessageFormat.format(message, args));
    }
    
    public void debug(String message) {
        log(Level.FINE, message);
    }
    public void debug(String message, Object... args) {
        debug(MessageFormat.format(message, args));
    }
}