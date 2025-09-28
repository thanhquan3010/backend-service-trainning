package vn.thanhquan.controller.request;

import lombok.Getter;

@Getter
public class UserPasswordRequest {
    private String id;
    private String password;
    private String confirmPassword;
}
