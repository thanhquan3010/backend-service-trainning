package vn.thanhquan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.thanhquan.model.Permission;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByMethodAndPath(String method, String path);
    List<Permission> findByCategory(String category);
    boolean existsByMethodAndPath(String method, String path);
}
