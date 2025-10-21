package vn.thanhquan.controller.response;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import vn.thanhquan.common.UserStatus;
import vn.thanhquan.common.UserType;

@Getter
@Setter
public class UserResponse implements Serializable {
    private Long id;
    private String firstName;
    private String lastName;
    private String gender;
    private Date birthday;
    private String username;
    private String email;
    private String phone;
    private UserType type;
    private UserStatus status;
    private Date createdAt;
    private Date updatedAt;
    private List<AddressResponse> addresses;
    
    @Getter
    @Setter
    public static class AddressResponse implements Serializable {
        private Long id;
        private String apartmentNumber;
        private String floor;
        private String building;
        private String streetNumber;
        private String street;
        private String city;
        private String country;
        private Integer addressType;
    }
}
