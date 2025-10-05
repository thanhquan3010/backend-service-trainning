package vn.thanhquan.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import vn.thanhquan.common.Gender;
import vn.thanhquan.common.UserType;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
public class UserCreationRequest implements Serializable {
    @NotBlank(message = "First name không được để trống")
    private String firstName;

    private String lastName;
    private Gender gender;
    private Date birthday;

    @NotBlank(message = "Username không được để trống")
    @Size(min = 4, message = "Username phải có ít nhất 4 ký tự")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    private String phone;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password phải có ít nhất 6 ký tự")
    private String password;

    private UserType type;
    private List<AddressRequest> addresses;
}