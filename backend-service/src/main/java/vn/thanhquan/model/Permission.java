// main/java/vn/thanhquan/model/Permission.java
package vn.thanhquan.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "tbl_permission")
public class Permission extends AbstractEntity {

    @Column(name = "method", nullable = false, length = 20)
    private String method; // Ví dụ: GET, POST, PUT, DELETE

    @Column(name = "path", nullable = false)
    private String path; // Ví dụ: /user/list, /user/{userId}

    @Column(name = "category", length = 100)
    private String category; // Ví dụ: User Management, Product Management

    @Column(name = "description")
    private String description;

    // Một Permission có thể thuộc về nhiều Role (thông qua RoleHasPermission)
    @OneToMany(mappedBy = "permission")
    private Set<RoleHasPermission> rolePermissions;
}