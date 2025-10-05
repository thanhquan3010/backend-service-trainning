// main/java/vn/thanhquan/model/Role.java
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
@Table(name = "tbl_role")
public class Role extends AbstractEntity {

    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    // Một Role có thể được gán cho nhiều User (thông qua UserHasRole)
    @OneToMany(mappedBy = "role")
    private Set<UserHasRole> userRoles;

    // Một Role có thể có nhiều Permission (thông qua RoleHasPermission)
    @OneToMany(mappedBy = "role")
    private Set<RoleHasPermission> rolePermissions;
}