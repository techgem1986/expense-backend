package com.expenseapp.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple rate limiting filter - 100 requests per minute per client
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final long TIME_WINDOW_MS = 60 * 1000; // 1 minute

    private final ConcurrentHashMap<String, ClientRequestInfo> clientRequests = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String clientKey = getClientKey(request);

        if (isRateLimited(clientKey)) {
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Rate limit exceeded. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(String clientKey) {
        long currentTime = System.currentTimeMillis();

        ClientRequestInfo info = clientRequests.computeIfAbsent(clientKey,
            k -> new ClientRequestInfo());

        synchronized (info) {
            // Reset counter if time window has passed
            if (currentTime - info.windowStart > TIME_WINDOW_MS) {
                info.requestCount.set(0);
                info.windowStart = currentTime;
            }

            // Check if limit exceeded
            if (info.requestCount.get() >= MAX_REQUESTS_PER_MINUTE) {
                return true;
            }

            // Increment counter
            info.requestCount.incrementAndGet();
            return false;
        }
    }

    /**
     * Get client key for rate limiting (IP address or user identifier)
     */
    private String getClientKey(HttpServletRequest request) {
        // Use X-Forwarded-For header if behind load balancer, otherwise remote address
        String clientIP = request.getHeader("X-Forwarded-For");
        if (clientIP == null || clientIP.isEmpty()) {
            clientIP = request.getRemoteAddr();
        }

        // If user is authenticated, use user identifier instead of IP
        String userEmail = request.getUserPrincipal() != null ?
                request.getUserPrincipal().getName() : null;

        return userEmail != null ? "user:" + userEmail : "ip:" + clientIP;
    }

    private static class ClientRequestInfo {
        private final AtomicLong requestCount = new AtomicLong(0);
        private volatile long windowStart = System.currentTimeMillis();
    }
}