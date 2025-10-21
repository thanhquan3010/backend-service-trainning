package vn.thanhquan.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final ConcurrentHashMap<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final long WINDOW_SIZE_MS = 60 * 1000; // 1 minute

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIpAddress(request);
        String endpoint = request.getRequestURI();
        String key = clientIp + ":" + endpoint;

        RateLimitInfo rateLimitInfo = rateLimitMap.computeIfAbsent(key, k -> new RateLimitInfo());

        long currentTime = System.currentTimeMillis();
        
        // Reset counter if window has passed
        if (currentTime - rateLimitInfo.getWindowStart() > WINDOW_SIZE_MS) {
            rateLimitInfo.setWindowStart(currentTime);
            rateLimitInfo.getRequestCount().set(0);
        }

        int currentCount = rateLimitInfo.getRequestCount().incrementAndGet();
        
        if (currentCount > MAX_REQUESTS_PER_MINUTE) {
            log.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, endpoint);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("{\"error\":\"Rate limit exceeded. Please try again later.\"}");
            response.setContentType("application/json");
            return false;
        }

        return true;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private static class RateLimitInfo {
        private long windowStart = System.currentTimeMillis();
        private final AtomicInteger requestCount = new AtomicInteger(0);

        public long getWindowStart() {
            return windowStart;
        }

        public void setWindowStart(long windowStart) {
            this.windowStart = windowStart;
        }

        public AtomicInteger getRequestCount() {
            return requestCount;
        }
    }
}
