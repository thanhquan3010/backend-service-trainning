// src/main/java/vn/thanhquan/repository/RoleRepository.java
package vn.thanhquan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.thanhquan.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}