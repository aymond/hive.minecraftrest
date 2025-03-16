package com.minecraftrest.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.mindrot.jbcrypt.BCrypt;
import spark.Request;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SecurityConfig {
    private final String jwtSecret;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final LoadingCache<String, Integer> requestCountsCache;
    private final int maxRequestsPerMinute;

    public SecurityConfig(String jwtSecret, int maxRequestsPerMinute) {
        this.jwtSecret = jwtSecret;
        this.algorithm = Algorithm.HMAC256(jwtSecret);
        this.verifier = JWT.require(algorithm).build();
        this.maxRequestsPerMinute = maxRequestsPerMinute;

        this.requestCountsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    public String generateToken(String username) {
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24 hours
                .sign(algorithm);
    }

    public boolean verifyToken(String token) {
        try {
            DecodedJWT jwt = verifier.verify(token);
            return !jwt.getExpiresAt().before(new Date());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    public boolean isRateLimited(Request request) {
        String ip = request.ip();
        int count = requestCountsCache.getUnchecked(ip);
        requestCountsCache.put(ip, count + 1);
        return count >= maxRequestsPerMinute;
    }

    public String extractToken(Request request) {
        String authHeader = request.headers("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
} 