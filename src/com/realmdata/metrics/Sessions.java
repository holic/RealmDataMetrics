package com.realmdata.metrics;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Sessions implements Listener {
    private static Sessions instance = null;
    protected static final Map<OfflinePlayer, UUID> sessions = new HashMap<OfflinePlayer, UUID>();
    
    protected Sessions() { }
    public static Sessions getInstance() {
        return instance == null
                ? instance = new Sessions()
                : instance;
    }
    
    public static UUID getSession(OfflinePlayer player) {
        return sessions.get(player);
    }
    public static UUID setSession(OfflinePlayer player, UUID uuid) {
        return uuid == null
                ? sessions.remove(player)
                : sessions.put(player, uuid);
    }
    
    public static UUID startSession(OfflinePlayer player) {
        UUID uuid = UUID.randomUUID();
        setSession(player, uuid);
        return uuid;
    }
    public static UUID endSession(OfflinePlayer player) {
        return setSession(player, null);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void playerLogin(PlayerLoginEvent event) {
        if(event.getResult() != PlayerLoginEvent.Result.ALLOWED) return;
        startSession(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void playerQuit(PlayerQuitEvent event) {
        endSession(event.getPlayer());
    }
    
}
