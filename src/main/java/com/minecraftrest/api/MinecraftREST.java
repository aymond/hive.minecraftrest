package com.minecraftrest.api;

import com.minecraftrest.api.service.ApiService;
import org.bukkit.plugin.java.JavaPlugin;

public final class MinecraftREST extends JavaPlugin {
    private ApiService apiService;
    private static final int DEFAULT_API_PORT = 4567;
    private static final int DEFAULT_MAX_REQUESTS = 60;

    @Override
    public void onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Get configuration values
        int apiPort = getConfig().getInt("api-port", DEFAULT_API_PORT);
        String jwtSecret = getConfig().getString("security.jwt-secret", "your-secret-key-here");
        int maxRequests = getConfig().getInt("security.max-requests-per-minute", DEFAULT_MAX_REQUESTS);
        String adminUsername = getConfig().getString("security.admin-credentials.username", "admin");
        String adminPassword = getConfig().getString("security.admin-credentials.password", "change-this-password");

        // Validate JWT secret
        if (jwtSecret.equals("your-secret-key-here")) {
            getLogger().warning("Using default JWT secret! Please change this in config.yml for security!");
        }

        // Initialize API service
        apiService = new ApiService(apiPort, jwtSecret, maxRequests, adminUsername, adminPassword);
        
        try {
            apiService.start();
            getLogger().info("API service started on port " + apiPort);
        } catch (Exception e) {
            getLogger().severe("Failed to start API service: " + e.getMessage());
            e.printStackTrace();
        }

        getLogger().info("MinecraftREST plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Shutdown API service
        if (apiService != null) {
            apiService.stop();
            getLogger().info("API service stopped");
        }
        
        getLogger().info("MinecraftREST plugin has been disabled!");
    }
} 