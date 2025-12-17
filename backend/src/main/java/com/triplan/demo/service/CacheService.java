package com.triplan.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import com.triplan.demo.dto.TripDTO;
import com.triplan.demo.model.User;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    private static final String USER_CACHE_PREFIX = "user:";
    private static final String TRIP_CACHE_PREFIX = "trip:";
    private static final String USER_TRIPS_PREFIX = "user_trips:";
    private static final long CACHE_TTL = 30; // 30 хвилин

    public CacheService(RedisService redisService, ObjectMapper objectMapper) {
        this.redisService = redisService;
        this.objectMapper = objectMapper;
    }

    // ========== User Cache ==========

    public void cacheUser(String email, User user) {
        try {
            String key = USER_CACHE_PREFIX + email;
            String json = objectMapper.writeValueAsString(user);
            redisService.save(key, json, CACHE_TTL, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            // Log error but don't fail
            System.err.println("Error caching user: " + e.getMessage());
        }
    }

    public User getCachedUser(String email) {
        try {
            String key = USER_CACHE_PREFIX + email;
            Object cached = redisService.get(key);
            if (cached != null) {
                return objectMapper.readValue(cached.toString(), User.class);
            }
        } catch (Exception e) {
            System.err.println("Error reading cached user: " + e.getMessage());
        }
        return null;
    }

    public void evictUser(String email) {
        String key = USER_CACHE_PREFIX + email;
        redisService.delete(key);
    }

    // ========== Trip Cache ==========

    public void cacheTrip(Long tripId, TripDTO trip) {
        try {
            String key = TRIP_CACHE_PREFIX + tripId;
            String json = objectMapper.writeValueAsString(trip);
            redisService.save(key, json, CACHE_TTL, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            System.err.println("Error caching trip: " + e.getMessage());
        }
    }

    public TripDTO getCachedTrip(Long tripId) {
        try {
            String key = TRIP_CACHE_PREFIX + tripId;
            Object cached = redisService.get(key);
            if (cached != null) {
                return objectMapper.readValue(cached.toString(), TripDTO.class);
            }
        } catch (Exception e) {
            System.err.println("Error reading cached trip: " + e.getMessage());
        }
        return null;
    }

    public void evictTrip(Long tripId) {
        String key = TRIP_CACHE_PREFIX + tripId;
        redisService.delete(key);
    }

    // ========== User Trips Cache ==========

    public void cacheUserTrips(Long userId, List<TripDTO> trips) {
        try {
            String key = USER_TRIPS_PREFIX + userId;
            String json = objectMapper.writeValueAsString(trips);
            redisService.save(key, json, CACHE_TTL, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            System.err.println("Error caching user trips: " + e.getMessage());
        }
    }

    public List<TripDTO> getCachedUserTrips(Long userId) {
        try {
            String key = USER_TRIPS_PREFIX + userId;
            Object cached = redisService.get(key);
            if (cached != null) {
                return objectMapper.readValue(
                        cached.toString(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, TripDTO.class)
                );
            }
        } catch (Exception e) {
            System.err.println("Error reading cached user trips: " + e.getMessage());
        }
        return null;
    }

    public void evictUserTrips(Long userId) {
        String key = USER_TRIPS_PREFIX + userId;
        redisService.delete(key);
    }

    // ========== Session Management ==========

    public void saveSession(String email, String token) {
        String key = "session:" + email;
        redisService.save(key, token, 24, TimeUnit.HOURS);
    }

    public boolean isSessionValid(String email) {
        String key = "session:" + email;
        return redisService.exists(key);
    }

    public void invalidateSession(String email) {
        String key = "session:" + email;
        redisService.delete(key);
    }

    // ========== Rate Limiting ==========

    public boolean checkRateLimit(String email, int maxRequests, int timeWindow) {
        String key = "rate_limit:" + email;

        if (!redisService.exists(key)) {
            redisService.save(key, "1", timeWindow, TimeUnit.SECONDS);
            return true;
        }

        Long currentCount = redisService.increment(key);
        return currentCount != null && currentCount <= maxRequests;
    }
}