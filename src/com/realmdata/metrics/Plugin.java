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

import org.bukkit.entity.Player;
import org.bukkit.entity.Creature;


public class Plugin extends JavaPlugin implements Listener {
    protected final Events events = new Events("iron.minecarts.com");
    
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(Sessions.getInstance(), this);
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void serverPing(final ServerListPingEvent event) {
        events.track("Server", "Ping", null, null, new HashMap<String, Object>() {{
            put("IP", event.getAddress().toString());
        }});
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void playerLogin(final PlayerLoginEvent event) {
        events.track("Server", "Login", null, null, new HashMap<String, Object>() {{
            put("Player", event.getPlayer().getName());
            put("Hostname", event.getHostname());
            put("Result", event.getResult().toString());
        }});
    }
    
    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        events.track("Player", "Join", event.getPlayer(), event.getPlayer().getLocation(), null);
    }
    
    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        events.track("Player", "Quit", event.getPlayer(), event.getPlayer().getLocation(), null);
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
                events.track("Player", "Block break", event.getPlayer(), event.getBlock().getLocation(), new HashMap<String, Object>() {{
                    put("Type", event.getBlock().getType().toString());
                }});
                break;
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void creatureSpawn(final CreatureSpawnEvent event) {
        events.track("Creature", "Spawn", null, event.getLocation(), new HashMap<String, Object>() {{
            put("Type", event.getEntity().getType().toString());
            put("Reason", event.getSpawnReason().toString());
        }});
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void entityDeath(final EntityDeathEvent event) {
        if(event.getEntity() instanceof Creature) {
            events.track("Creature", "Death", null, event.getEntity().getLocation(), new HashMap<String, Object>() {{
                put("Type", event.getEntity().getType().toString());
                put("Ticks lived", event.getEntity().getTicksLived());
                put("Exp dropped", event.getDroppedExp());
                if(event.getEntity().getLastDamageCause() != null) {
                    put("Cause", event.getEntity().getLastDamageCause().getCause().toString());
                }
            }});
        }
        else if(event.getEntity() instanceof Player) {
            events.track("Player", "Death", (Player) event.getEntity(), event.getEntity().getLocation(), new HashMap<String, Object>() {{
                put("Cause", event.getEntity().getLastDamageCause().getCause().toString());
                put("Ticks lived", event.getEntity().getTicksLived());
                put("Exp dropped", event.getDroppedExp());
            }});
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void entityExplode(final EntityExplodeEvent event) {
        switch(event.getEntity().getType()) {
            case CREEPER:
            case PRIMED_TNT:
                events.track("Creature", "Explode", null, event.getEntity().getLocation(), new HashMap<String, Object>() {{
                    put("Type", event.getEntity().getType().toString());
                    put("Ticks lived", event.getEntity().getTicksLived());
                    put("Blocks exploded", event.blockList().size());
                    put("Yield", event.getYield());
                }});
                break;
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void lightningStrike(final LightningStrikeEvent event) {
        events.track("World", "Lightning strike", null, event.getLightning().getLocation(), null);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void structureGrow(final StructureGrowEvent event) {
        events.track(event.getPlayer() == null ? "World" : "Player", "Grow tree", event.getPlayer(), event.getLocation(), new HashMap<String, Object>() {{
            put("Type", event.getSpecies().toString());
        }});
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