package vn.thanhquan.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import vn.thanhquan.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import vn.thanhquan.controller.request.SigninRequest;
import vn.thanhquan.controller.response.TokenResponse;
import vn.thanhquan.service.AuthenticationService;
import vn.thanhquan.service.JwtService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.Collection;
import java.util.Collections;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public AuthenticationServiceImpl(JwtService jwtService,
            AuthenticationManager authenticationManager,
            UserRepository userRepository) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    // TODO: Replace with actual user validation and authorities retrieval
    private Collection<? extends GrantedAuthority> getAuthoritiesForUser(String username) {
        return Collections.emptyList();
    }

    @Override
    public TokenResponse getAccessToken(SigninRequest requestToken) {
        try {
            String email = requestToken.getEmail();
            String password = requestToken.getPassword();

            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(email, password);

            Authentication authentication = authenticationManager.authenticate(authRequest);

            // principal may be a UserDetails (spring's User) or a custom UserEntity
            String username;
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.User) {
                username = ((org.springframework.security.core.userdetails.User) principal).getUsername();
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }

            // lookup real user id from repository
            long userId = userRepository.findByUsername(username)
                    .map(u -> u.getId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            String accessToken = jwtService.generateAccessToken(userId, username, authorities);
            String refreshToken = jwtService.generateRefreshToken(userId, username);

            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (AuthenticationException ex) {
            throw new RuntimeException("Authentication failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public TokenResponse getRefreshToken(String refreshToken) {
        // 1. Extract username from refresh token using REFRESH_TOKEN type
        String username = jwtService.extractUsername(refreshToken, vn.thanhquan.common.TokenType.REFRESH_TOKEN);
        if (username == null) {
            throw new RuntimeException("Invalid refresh token: username not found");
        }

        // 2. Find user by username
        return userRepository.findByUsername(username)
                .map(user -> {
                    long userId = user.getId();
                    Collection<? extends GrantedAuthority> authorities;
                    try {
                        authorities = user.getAuthorities();
                    } catch (Exception e) {
                        authorities = Collections.emptyList();
                    }
                    // 3. Generate new access token
                    String newAccessToken = jwtService.generateAccessToken(userId, username, authorities);
                    // 4. Return TokenResponse
                    return TokenResponse.builder()
                            .accessToken(newAccessToken)
                            .refreshToken(refreshToken)
                            .build();
                })
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}
