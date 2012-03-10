package com.realmdata.metrics;

import java.util.logging.Logger;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONAware;

import org.bukkit.entity.Player;
import org.bukkit.Location;


public class Events {
    protected static final Logger logger = Logger.getLogger(Events.class.getCanonicalName());
    protected static final String endpoint = "http://api.realmdata.com/metrics/v1/%s/events";
    
    protected final String profile;
    protected final URL url;
    
    protected final JSONArray events = new JSONArray();
    
    
    public Events(String profile) {
        this.profile = profile;
        
        try {
            this.url = new URL(String.format(endpoint, profile));
        }
        catch(MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        
        
        new Thread() {
            public void run() {
                while(true) {
                    try {
                        sleep(2000);
                    }
                    catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                    
                    JSONArray batch;
                    synchronized(events) {
                        if(events.isEmpty()) {
                            continue;
                        }
                        else {
                            batch = new JSONArray();
                            batch.addAll(events);
                            events.clear();
                        }
                    }
                    
                    Date start = new Date();

                    HttpURLConnection conn = null;
                    try {
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");

                        conn.setDoOutput(true);
                        conn.setDoInput(true);
                        
                        OutputStreamWriter or = new OutputStreamWriter(conn.getOutputStream());
                        String json = batch.toJSONString();
                        or.write(json);
                        or.flush();
                        or.close();

                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream()));
                        String line;
                        List<String> lines = new ArrayList<String>();
                        while((line = br.readLine()) != null) {
                            lines.add(line);
                        }
                        br.close();


                        logger.info(String.format("Got response code: %d", conn.getResponseCode()));
                        if(conn.getResponseCode() == 200) {
                            logger.info(String.format("Successfully sent %d events", batch.size()));
                        }
                        else {
                            logger.info(StringUtils.join(lines, "\n"));
                            logger.info(json);
                        }
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if(conn != null) {
                            conn.disconnect();
                        }
                    }

                    logger.info(String.format("Response took %d ms", new Date().getTime() - start.getTime()));
                }
            }
        }.start();
    }
    
    
    
    public void track(final String category, final String name, final Player player, final Location location, final Map<String, Object> meta) {
        if(name == null) throw new IllegalArgumentException("Event name must not be null");
        
        events.add(new JSONObject() {{
            put("time", ((double) new Date().getTime()) / 1000);
            put("name", name);
            if(category != null) {
                put("category", category);
            }
            if(meta != null) {
                JSONArray metaArray = new JSONArray();
                for(final String key : meta.keySet()) {
                    metaArray.add(new JSONObject() {{
                        put("name", key);
                        put("value", meta.get(key));
                    }});
                }
                put("meta", metaArray);
            }
            if(player != null) {
                put("player", new JSONObject() {{
                    put("session", Sessions.getSession(player).toString());
                    put("name", player.getName());
                    put("ip", player.getAddress().getAddress().toString());
                }});
            }
            if(location != null) {
                put("location", new JSONObject() {{
                    put("map", location.getWorld().getName());
                    put("x", location.getX());
                    put("y", location.getZ());
                    put("z", location.getY());
                }});
            }
        }});
    }
    
    
}
