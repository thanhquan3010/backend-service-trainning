package vn.thanhquan.service;

import vn.thanhquan.model.Role;
import vn.thanhquan.model.Permission;
import vn.thanhquan.model.UserEntity;

import java.util.List;
import java.util.Set;

public interface RoleService {
    
    // Role management
    Role createRole(String name, String description);
    Role updateRole(Long roleId, String name, String description);
    void deleteRole(Long roleId);
    List<Role> getAllRoles();
    Role getRoleById(Long roleId);
    Role getRoleByName(String name);
    
    // Permission management
    Permission createPermission(String method, String path, String category, String description);
    Permission updatePermission(Long permissionId, String method, String path, String category, String description);
    void deletePermission(Long permissionId);
    List<Permission> getAllPermissions();
    Permission getPermissionById(Long permissionId);
    List<Permission> getPermissionsByCategory(String category);
    
    // Role-Permission mapping
    void assignPermissionToRole(Long roleId, Long permissionId);
    void removePermissionFromRole(Long roleId, Long permissionId);
    Set<Permission> getRolePermissions(Long roleId);
    
    // User-Role mapping
    void assignRoleToUser(Long userId, Long roleId);
    void removeRoleFromUser(Long userId, Long roleId);
    Set<Role> getUserRoles(Long userId);
    Set<Permission> getUserPermissions(Long userId);
    
    // Initialize default roles and permissions
    void initializeDefaultRolesAndPermissions();
}
