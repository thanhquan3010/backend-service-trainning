package vn.thanhquan.service;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import vn.thanhquan.common.TokenType;

public interface JwtService {

    String generateAccessToken(long userId, String username, Collection<? extends GrantedAuthority> authorities);

    String generateRefreshToken(long userId, String username);

    String extractUsername(String token, TokenType tokenType);
}
