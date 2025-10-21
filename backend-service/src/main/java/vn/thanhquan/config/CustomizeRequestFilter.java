package vn.thanhquan.config; // Thay đổi package cho phù hợp

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import vn.thanhquan.controller.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import vn.thanhquan.common.TokenType; // Giả sử bạn có enum này
import vn.thanhquan.service.JwtService;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "JWT_REQUEST_FILTER")
public class CustomizeRequestFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Lấy header "Authorization" từ request
        final String authHeader = request.getHeader("Authorization");

        // 2. Nếu không có header hoặc không phải là "Bearer Token", cho request đi tiếp
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Tách chuỗi JWT ra khỏi "Bearer "
        final String jwt = authHeader.substring(7);
        final String username;

        // 4. Trích xuất username từ JWT
        try {
            username = jwtService.extractUsername(jwt, TokenType.ACCESS_TOKEN);
        } catch (Exception e) {
            log.error("Cannot extract username from JWT: {}", e.getMessage());
            // Trả về lỗi dạng JSON
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or expired JWT token",
                    request.getRequestURI());
            ObjectMapper mapper = new ObjectMapper();
            response.getWriter().write(mapper.writeValueAsString(errorResponse));
            return;
        }

        // 5. Nếu có username và người dùng chưa được xác thực trong SecurityContext
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Tải thông tin người dùng từ database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails, TokenType.ACCESS_TOKEN)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.info("User '{}' authenticated successfully.", username);
            } else {
                log.warn("JWT token is not valid for user '{}'", username);
            }
        }

        // 6. Cho phép request đi tiếp đến filter tiếp theo
        filterChain.doFilter(request, response);
    }

}