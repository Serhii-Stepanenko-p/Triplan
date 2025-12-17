package com.triplan.demo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.triplan.demo.service.CacheService;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final CacheService cacheService;
    private static final int MAX_REQUESTS = 100; // максимум 100 запитів
    private static final int TIME_WINDOW = 60; // за 60 секунд

    public RateLimitInterceptor(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Отримуємо email користувача з Security Context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() &&
                !auth.getPrincipal().equals("anonymousUser")) {

            String email = auth.getName();

            // Перевіряємо rate limit
            if (!cacheService.checkRateLimit(email, MAX_REQUESTS, TIME_WINDOW)) {
                response.setStatus(429); // Too Many Requests
                response.getWriter().write("Rate limit exceeded. Please try again later.");
                return false;
            }
        }

        return true;
    }
}