// main/java/vn/thanhquan/model/Group.java
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
@Table(name = "tbl_group")
public class Group extends AbstractEntity {

    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    // Một Group có nhiều User (thông qua GroupHasUser)
    @OneToMany(mappedBy = "group")
    private Set<GroupHasUser> groupUsers;
}