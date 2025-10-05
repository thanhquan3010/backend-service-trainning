package vn.thanhquan.controller.request;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import vn.thanhquan.common.Gender;
import vn.thanhquan.common.UserType;

@Getter

public class UserCreationRequest implements Serializable {
    private String firstName;
    private String lastName;
    private Gender gender;
    private Date birthday;
    private String username;
    private String email;
    private String phone;
    private String password;
    private UserType type;
    private List<AddressRequest> addresses; // home , office
}
