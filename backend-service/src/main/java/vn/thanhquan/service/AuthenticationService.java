package vn.thanhquan.service;

import vn.thanhquan.controller.request.SigninRequest;
import vn.thanhquan.controller.response.TokenResponse;

public interface AuthenticationService {
    TokenResponse getAccessToken(SigninRequest requestToken);

    TokenResponse getRefreshToken(String refreshToken);
}
