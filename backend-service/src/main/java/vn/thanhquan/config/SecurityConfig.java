package vn.thanhquan.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for the application.
 * Configures JWT authentication, CORS, security headers, and endpoint authorization.
 *
 * @author Backend Team
 * @version 1.0
 * @since 2025-10-21
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            org.springframework.security.authentication.AuthenticationProvider authenticationProvider,
            CustomizeRequestFilter customizeRequestFilter
    ) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Security headers
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'")
                )
                .frameOptions(frame -> frame.deny())
                .xssProtection(xss -> xss
                    .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                )
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
            )
            
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - no authentication required
                .requestMatchers("/", "/index.html", "/error").permitAll()
                .requestMatchers("/auth/**").permitAll()
                
                // Actuator endpoints - restricted access
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasAuthority("ADMIN")
                
                // Swagger/OpenAPI endpoints
                .requestMatchers("/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/swagger-resources/**", "/webjars/**").permitAll()
                
                // H2 Console (only for dev/test)
                .requestMatchers("/h2-console/**").permitAll()
                
                // Public user endpoints
                .requestMatchers("/role/initialize").permitAll()
                .requestMatchers("/user/confirm-email").permitAll()
                
                // Protected user endpoints
                .requestMatchers("/user/list").hasAnyAuthority("ADMIN", "USER")
                .requestMatchers("/user/{id}").hasAnyAuthority("ADMIN", "USER")
                .requestMatchers("/user/add").hasAuthority("ADMIN")
                .requestMatchers("/user/update").hasAnyAuthority("ADMIN", "USER")
                .requestMatchers("/user/change-pwd").hasAnyAuthority("ADMIN", "USER")
                .requestMatchers("/user/{id}/del").hasAuthority("ADMIN")
                
                // Role endpoints
                .requestMatchers("/role/**").hasAuthority("ADMIN")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(customizeRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures CORS settings using environment variables.
     * Allows configuration of allowed origins per environment.
     *
     * @return CorsConfigurationSource with configured CORS settings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Parse allowed origins from environment variable
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOriginPatterns(origins);
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
