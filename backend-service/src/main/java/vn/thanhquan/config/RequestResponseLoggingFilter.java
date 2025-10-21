package vn.thanhquan.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Filter to log all HTTP requests and responses.
 * Provides detailed logging for debugging and monitoring purposes.
 * 
 * Features:
 * - Logs request method, URI, and client IP
 * - Logs response status and execution time
 * - Excludes sensitive endpoints from detailed logging
 * 
 * @author Backend Team
 * @version 1.0
 * @since 2025-10-21
 */
@Slf4j
@Component
@Order(1)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/actuator",
        "/h2-console",
        "/swagger-ui",
        "/v3/api-docs",
        "/webjars"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip logging for excluded paths
        if (shouldSkipLogging(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
        
        // Wrap request and response for content caching
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        // Log request details
        logRequest(requestWrapper);

        try {
            // Continue with the filter chain
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            // Calculate execution time
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log response details
            logResponse(responseWrapper, executionTime);
            
            // Copy response content back to the original response
            responseWrapper.copyBodyToResponse();
        }
    }

    /**
     * Logs incoming request details.
     */
    private void logRequest(ContentCachingRequestWrapper request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = getClientIp(request);

        if (queryString != null) {
            uri += "?" + queryString;
        }

        log.info("→ Incoming Request: {} {} from {} | User-Agent: {}",
                method,
                uri,
                clientIp,
                request.getHeader("User-Agent"));

        // Log request headers (excluding sensitive ones)
        if (log.isDebugEnabled()) {
            log.debug("Request Headers: {}", getHeadersAsString(request));
        }
    }

    /**
     * Logs outgoing response details.
     */
    private void logResponse(ContentCachingResponseWrapper response, long executionTime) {
        int status = response.getStatus();
        String statusText = getStatusText(status);

        log.info("← Outgoing Response: {} {} | Execution Time: {}ms",
                status,
                statusText,
                executionTime);

        // Log response body for errors (4xx, 5xx)
        if (log.isDebugEnabled() || status >= 400) {
            String responseBody = getResponseBody(response);
            if (responseBody != null && !responseBody.isEmpty()) {
                log.warn("Response Body: {}", responseBody);
            }
        }

        // Log slow requests (> 1 second)
        if (executionTime > 1000) {
            log.warn("⚠️ Slow Request Detected: {}ms", executionTime);
        }
    }

    /**
     * Determines if logging should be skipped for the given path.
     */
    private boolean shouldSkipLogging(String uri) {
        return EXCLUDED_PATHS.stream().anyMatch(uri::startsWith);
    }

    /**
     * Gets the real client IP address, considering proxy headers.
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // If multiple IPs, take the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * Gets response body as string.
     */
    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return null;
    }

    /**
     * Gets request headers as a formatted string.
     */
    private String getHeadersAsString(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            // Skip sensitive headers
            if (!headerName.equalsIgnoreCase("Authorization") && 
                !headerName.equalsIgnoreCase("Cookie")) {
                headers.append(headerName)
                       .append(": ")
                       .append(request.getHeader(headerName))
                       .append("; ");
            }
        });
        return headers.toString();
    }

    /**
     * Gets human-readable status text.
     */
    private String getStatusText(int status) {
        if (status >= 200 && status < 300) return "✓ Success";
        if (status >= 300 && status < 400) return "→ Redirect";
        if (status >= 400 && status < 500) return "⚠ Client Error";
        if (status >= 500) return "✗ Server Error";
        return "Unknown";
    }
}

