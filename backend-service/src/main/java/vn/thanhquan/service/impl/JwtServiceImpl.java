package vn.thanhquan.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import vn.thanhquan.common.TokenType;
import vn.thanhquan.service.JwtService;

@Service
@Slf4j(topic = "JWT-SERVICE")
public class JwtServiceImpl implements JwtService {

    // --- 1. Key Management từ file thứ hai (ĐÚNG CHUẨN) ---

    @Value("${jwt.secret.access-key}")
    private String accessTokenSecretString;

    @Value("${jwt.secret.refresh-key}")
    private String refreshTokenSecretString;

    private SecretKey ACCESS_TOKEN_KEY;
    private SecretKey REFRESH_TOKEN_KEY;

    // Thời gian hết hạn
    private static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000; // 15 phút
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 ngày

    @jakarta.annotation.PostConstruct
    public void init() {
        byte[] accessTokenBytes = Decoders.BASE64.decode(accessTokenSecretString);
        this.ACCESS_TOKEN_KEY = Keys.hmacShaKeyFor(accessTokenBytes);

        byte[] refreshTokenBytes = Decoders.BASE64.decode(refreshTokenSecretString);
        this.REFRESH_TOKEN_KEY = Keys.hmacShaKeyFor(refreshTokenBytes);
    }

    // --- 2. Các phương thức Generate Token từ file thứ nhất (CHI TIẾT HƠN) ---

    @Override
    public String generateAccessToken(long userId, String username,
            Collection<? extends GrantedAuthority> authorities) {
        log.info("Generating access token for userId: {}, username: {}", userId, username);
        String authoritiesStr = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Map<String, Object> claims = Map.of(
                "userId", userId,
                "authorities", authoritiesStr);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(ACCESS_TOKEN_KEY) // <-- Dùng Access Key
                .compact();
    }

    @Override
    public String generateRefreshToken(long userId, String username) {
        log.info("Generating refresh token for userId: {}, username: {}", userId, username);
        Map<String, Object> claims = Map.of("userId", userId);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(REFRESH_TOKEN_KEY) // <-- Dùng Refresh Key
                .compact();
    }

    // --- 3. Các phương thức trích xuất và xác thực (MERGE & TỐI ƯU) ---

    private Claims extractAllClaims(String token, SecretKey key) {
        log.info("Extracting claims from token");
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, SecretKey key, Function<Claims, T> claimsResolver) {
        log.info("Extracting claim using provided resolver");
        final Claims claims = extractAllClaims(token, key);
        return claimsResolver.apply(claims);
    }

    @Override
    public String extractUsername(String token, TokenType tokenType) {
        log.info("Extracting username from {} token", tokenType);
        SecretKey key = (tokenType == TokenType.ACCESS_TOKEN) ? ACCESS_TOKEN_KEY : REFRESH_TOKEN_KEY;
        try {
            return extractClaim(token, key, Claims::getSubject);
        } catch (Exception e) {
            log.error("Failed to extract username from {} token: {}", tokenType, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails, TokenType tokenType) {
        final String username = extractUsername(token, tokenType);
        SecretKey key = (tokenType == TokenType.ACCESS_TOKEN) ? ACCESS_TOKEN_KEY : REFRESH_TOKEN_KEY;
        return (username != null && username.equals(userDetails.getUsername())) && !isTokenExpired(token, key);
    }

    public boolean isTokenExpired(String token, SecretKey key) {
        log.info("Checking if token is expired");
        try {
            return extractClaim(token, key, Claims::getExpiration).before(new Date());
        } catch (Exception e) {
            log.error("Failed to check token expiration: {}", e.getMessage());
            return true; // Coi như hết hạn nếu có lỗi
        }
    }

}