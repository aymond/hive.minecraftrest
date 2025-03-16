package com.minecraftrest.api.service;

import com.google.gson.Gson;
import com.minecraftrest.api.security.SecurityConfig;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import spark.Spark;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ApiService {
    private final int port;
    private final Gson gson;
    private final SecurityConfig securityConfig;
    private final String adminUsername;
    private final String adminPasswordHash;

    public ApiService(int port, String jwtSecret, int maxRequestsPerMinute, 
                     String adminUsername, String adminPassword) {
        this.port = port;
        this.gson = new Gson();
        this.securityConfig = new SecurityConfig(jwtSecret, maxRequestsPerMinute);
        this.adminUsername = adminUsername;
        this.adminPasswordHash = securityConfig.hashPassword(adminPassword);
    }

    private boolean authenticate(spark.Request request) {
        String token = securityConfig.extractToken(request);
        return token != null && securityConfig.verifyToken(token);
    }

    public void start() {
        Spark.port(port);
        
        // Configure CORS
        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
            
            // Rate limiting
            if (securityConfig.isRateLimited(request)) {
                Spark.halt(429, gson.toJson(Map.of("error", "Too many requests")));
            }
        });

        // Handle OPTIONS requests
        Spark.options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        // Authentication endpoint
        Spark.post("/api/auth/login", (request, response) -> {
            response.type("application/json");
            Map<String, String> body = gson.fromJson(request.body(), new com.google.gson.reflect.TypeToken<Map<String, String>>(){}.getType());
            String username = body.get("username");
            String password = body.get("password");

            if (username == null || password == null) {
                response.status(400);
                return gson.toJson(Map.of("error", "Username and password are required"));
            }

            if (username.equals(adminUsername) && securityConfig.checkPassword(password, adminPasswordHash)) {
                String token = securityConfig.generateToken(username);
                return gson.toJson(Map.of("token", token));
            } else {
                response.status(401);
                return gson.toJson(Map.of("error", "Invalid credentials"));
            }
        });

        // Protected GET endpoints
        Spark.get("/api/players", (request, response) -> {
            response.type("application/json");
            if (!authenticate(request)) {
                response.status(401);
                return gson.toJson(Map.of("error", "Unauthorized"));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("online_players", Bukkit.getOnlinePlayers()
                    .stream()
                    .map(player -> {
                        Map<String, Object> playerInfo = new HashMap<>();
                        playerInfo.put("name", player.getName());
                        playerInfo.put("uuid", player.getUniqueId().toString());
                        playerInfo.put("gamemode", player.getGameMode().toString());
                        playerInfo.put("health", player.getHealth());
                        playerInfo.put("level", player.getLevel());
                        return playerInfo;
                    })
                    .collect(Collectors.toList()));
            result.put("max_players", Bukkit.getMaxPlayers());
            return gson.toJson(result);
        });

        Spark.get("/api/server/info", (request, response) -> {
            response.type("application/json");
            if (!authenticate(request)) {
                response.status(401);
                return gson.toJson(Map.of("error", "Unauthorized"));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("version", Bukkit.getVersion());
            result.put("bukkit_version", Bukkit.getBukkitVersion());
            result.put("server_name", Bukkit.getName());
            result.put("online_mode", Bukkit.getOnlineMode());
            result.put("max_players", Bukkit.getMaxPlayers());
            result.put("current_players", Bukkit.getOnlinePlayers().size());
            result.put("worlds", Bukkit.getWorlds().stream()
                    .map(world -> Map.of(
                            "name", world.getName(),
                            "player_count", world.getPlayers().size(),
                            "time", world.getTime(),
                            "weather", world.hasStorm() ? "stormy" : "clear"
                    ))
                    .collect(Collectors.toList()));
            return gson.toJson(result);
        });

        // Protected POST endpoints
        Spark.post("/api/broadcast", (request, response) -> {
            response.type("application/json");
            if (!authenticate(request)) {
                response.status(401);
                return gson.toJson(Map.of("error", "Unauthorized"));
            }

            Map<String, String> body = gson.fromJson(request.body(), new com.google.gson.reflect.TypeToken<Map<String, String>>(){}.getType());
            String message = body.get("message");
            
            if (message == null || message.trim().isEmpty()) {
                response.status(400);
                return gson.toJson(Map.of("error", "Message is required"));
            }

            Bukkit.broadcastMessage(message);
            return gson.toJson(Map.of("success", true, "message", "Broadcast sent successfully"));
        });

        Spark.post("/api/player/message", (request, response) -> {
            response.type("application/json");
            if (!authenticate(request)) {
                response.status(401);
                return gson.toJson(Map.of("error", "Unauthorized"));
            }

            Map<String, String> body = gson.fromJson(request.body(), new com.google.gson.reflect.TypeToken<Map<String, String>>(){}.getType());
            String playerName = body.get("player");
            String message = body.get("message");

            if (playerName == null || message == null) {
                response.status(400);
                return gson.toJson(Map.of("error", "Player name and message are required"));
            }

            Player player = Bukkit.getPlayer(playerName);
            if (player == null) {
                response.status(404);
                return gson.toJson(Map.of("error", "Player not found"));
            }

            player.sendMessage(message);
            return gson.toJson(Map.of("success", true, "message", "Message sent successfully"));
        });

        // New endpoints for player management
        Spark.post("/api/player/kick", (request, response) -> {
            response.type("application/json");
            if (!authenticate(request)) {
                response.status(401);
                return gson.toJson(Map.of("error", "Unauthorized"));
            }

            Map<String, String> body = gson.fromJson(request.body(), new com.google.gson.reflect.TypeToken<Map<String, String>>(){}.getType());
            String playerName = body.get("player");
            String reason = body.get("reason");
            
            if (playerName == null || playerName.trim().isEmpty()) {
                response.status(400);
                return gson.toJson(Map.of("error", "Player name is required"));
            }

            Player player = Bukkit.getPlayer(playerName);
            if (player == null) {
                response.status(404);
                return gson.toJson(Map.of("error", "Player not found"));
            }

            // Run kick operation on the main server thread
            org.bukkit.scheduler.BukkitRunnable task = new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    player.kickPlayer(reason != null ? reason : "Kicked by admin");
                    response.status(200);
                    response.body(gson.toJson(Map.of("success", true, "message", "Player kicked successfully")));
                }
            };
            task.runTask(org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(ApiService.class));
            
            // This return is just a placeholder, the actual response is set in the BukkitRunnable
            return "";
        });

        Spark.post("/api/player/gamemode", (request, response) -> {
            response.type("application/json");
            if (!authenticate(request)) {
                response.status(401);
                return gson.toJson(Map.of("error", "Unauthorized"));
            }

            Map<String, String> body = gson.fromJson(request.body(), new com.google.gson.reflect.TypeToken<Map<String, String>>(){}.getType());
            String playerName = body.get("player");
            String gamemode = body.get("gamemode");

            if (playerName == null || gamemode == null) {
                response.status(400);
                return gson.toJson(Map.of("error", "Player name and gamemode are required"));
            }

            Player player = Bukkit.getPlayer(playerName);
            if (player == null) {
                response.status(404);
                return gson.toJson(Map.of("error", "Player not found"));
            }

            try {
                GameMode newGameMode = GameMode.valueOf(gamemode.toUpperCase());
                
                // Run gamemode change on the main server thread
                org.bukkit.scheduler.BukkitRunnable task = new org.bukkit.scheduler.BukkitRunnable() {
                    @Override
                    public void run() {
                        player.setGameMode(newGameMode);
                        response.status(200);
                        response.body(gson.toJson(Map.of("success", true, "message", "Gamemode changed successfully")));
                    }
                };
                task.runTask(org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(ApiService.class));
                
                // This return is just a placeholder, the actual response is set in the BukkitRunnable
                return "";
            } catch (IllegalArgumentException e) {
                response.status(400);
                return gson.toJson(Map.of("error", "Invalid gamemode"));
            }
        });

        Spark.post("/api/server/command", (request, response) -> {
            response.type("application/json");
            if (!authenticate(request)) {
                response.status(401);
                return gson.toJson(Map.of("error", "Unauthorized"));
            }

            Map<String, String> body = gson.fromJson(request.body(), new com.google.gson.reflect.TypeToken<Map<String, String>>(){}.getType());
            String command = body.get("command");

            if (command == null || command.trim().isEmpty()) {
                response.status(400);
                return gson.toJson(Map.of("error", "Command is required"));
            }

            // Run the command on the main server thread for thread safety
            org.bukkit.scheduler.BukkitRunnable task = new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    response.status(success ? 200 : 500);
                    response.body(gson.toJson(Map.of("success", success, "message", success ? "Command executed successfully" : "Command failed")));
                }
            };
            task.runTask(org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(ApiService.class));
            
            // This return is just a placeholder, the actual response is set in the BukkitRunnable
            return "";
        });

        // Add a shutdown hook to ensure Spark is stopped when the server stops
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void stop() {
        Spark.stop();
    }
} 