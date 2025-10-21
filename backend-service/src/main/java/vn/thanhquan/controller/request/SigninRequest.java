package vn.thanhquan.controller.request;

import java.io.Serializable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SigninRequest implements Serializable {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;
    
    @NotBlank(message = "Password không được để trống")
    private String password;
    
    private String platform;
    private String deviceToken;
    private String versionApp;
}
