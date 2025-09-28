package vn.thanhquan.controller.request;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;

import vn.thanhquan.common.Gender;

@Getter
public class UserUpdateRequest implements Serializable {
    private Long id;
    private String firstName;
    private String lastName;
    private Gender gender;
    private Date birthday;
    private String username;
    private String email;
    private String phone;
    private List<AddressRequest> addressRequests;
}