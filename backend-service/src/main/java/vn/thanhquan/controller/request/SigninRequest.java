package vn.thanhquan.controller.request;

import java.io.Serializable;

import lombok.Getter;

@Getter

public class SigninRequest implements Serializable {
    private String email;
    private String password;
    private String platform;
    private String deviceToken;
    private String versionApp;
}
