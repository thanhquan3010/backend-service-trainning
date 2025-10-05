package vn.thanhquan.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import vn.thanhquan.common.Gender;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
public class UserUpdateRequest implements Serializable {

    @NotNull(message = "ID không được để trống")
    private Long id;

    @NotBlank(message = "First name không được để trống")
    private String firstName;

    private String lastName;
    private Gender gender;
    private Date birthday;

    @NotBlank(message = "Username không được để trống")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    private String phone;
    private List<AddressRequest> addressRequests;
}