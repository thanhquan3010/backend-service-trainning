package vn.thanhquan.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;
import vn.thanhquan.common.Gender;
import vn.thanhquan.common.UserStatus;
import vn.thanhquan.common.UserType;

// Thêm các annotation của Hibernate để tự động quản lý ngày tháng
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@Entity
@Table(name = "tbl_user")
public class UserEntity implements Serializable, UserDetails {

    // Add roles field
    // You may need to import List and UserHasRole
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<UserHasRole> roles;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", unique = true, nullable = false, length = 255)
    private String firstName;

    @Column(name = "last_name", length = 255)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender")
    private Gender gender;

    @Temporal(TemporalType.DATE) // Chỉ lưu trữ ngày, không có giờ
    @Column(name = "date_of_birth")
    private Date birthday;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(unique = true, length = 20)
    private String phone;

    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING) // Lưu trữ giá trị enum dưới dạng chuỗi (ví dụ: "ADMIN", "USER")
    @Column(length = 50)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserType type;

    @Enumerated(EnumType.STRING) // Lưu trữ giá trị enum dưới dạng chuỗi (ví dụ: "ACTIVE", "INACTIVE")
    @Column(length = 50)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserStatus status;

    @CreationTimestamp // Tự động gán ngày giờ khi tạo mới record
    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @UpdateTimestamp // Tự động cập nhật ngày giờ khi record được cập nhật
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<Role> roleList = roles.stream().map(UserHasRole::getRole).toList();

        List<String> roleNames = roleList.stream().map(Role::getName).toList();

        return roleNames.stream().map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
