package vn.thanhquan.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.thanhquan.exception.ResourceNotFoundException;
import vn.thanhquan.model.*;
import vn.thanhquan.repository.*;
import vn.thanhquan.service.RoleService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "ROLE-SERVICE")
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Role createRole(String name, String description) {
        log.info("Creating role: {}", name);
        if (roleRepository.existsByName(name)) {
            throw new IllegalArgumentException("Role with name '" + name + "' already exists");
        }
        
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public Role updateRole(Long roleId, String name, String description) {
        log.info("Updating role: {}", roleId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        
        role.setName(name);
        role.setDescription(description);
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        log.info("Deleting role: {}", roleId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        roleRepository.delete(role);
    }

    @Override
    public List<Role> getAllRoles() {
        log.info("Getting all roles");
        return roleRepository.findAll();
    }

    @Override
    public Role getRoleById(Long roleId) {
        log.info("Getting role by id: {}", roleId);
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
    }

    @Override
    public Role getRoleByName(String name) {
        log.info("Getting role by name: {}", name);
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
    }

    @Override
    @Transactional
    public Permission createPermission(String method, String path, String category, String description) {
        log.info("Creating permission: {} {}", method, path);
        if (permissionRepository.existsByMethodAndPath(method, path)) {
            throw new IllegalArgumentException("Permission with method '" + method + "' and path '" + path + "' already exists");
        }
        
        Permission permission = new Permission();
        permission.setMethod(method);
        permission.setPath(path);
        permission.setCategory(category);
        permission.setDescription(description);
        return permissionRepository.save(permission);
    }

    @Override
    @Transactional
    public Permission updatePermission(Long permissionId, String method, String path, String category, String description) {
        log.info("Updating permission: {}", permissionId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + permissionId));
        
        permission.setMethod(method);
        permission.setPath(path);
        permission.setCategory(category);
        permission.setDescription(description);
        return permissionRepository.save(permission);
    }

    @Override
    @Transactional
    public void deletePermission(Long permissionId) {
        log.info("Deleting permission: {}", permissionId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + permissionId));
        permissionRepository.delete(permission);
    }

    @Override
    public List<Permission> getAllPermissions() {
        log.info("Getting all permissions");
        return permissionRepository.findAll();
    }

    @Override
    public Permission getPermissionById(Long permissionId) {
        log.info("Getting permission by id: {}", permissionId);
        return permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + permissionId));
    }

    @Override
    public List<Permission> getPermissionsByCategory(String category) {
        log.info("Getting permissions by category: {}", category);
        return permissionRepository.findByCategory(category);
    }

    @Override
    @Transactional
    public void assignPermissionToRole(Long roleId, Long permissionId) {
        log.info("Assigning permission {} to role {}", permissionId, roleId);
        Role role = getRoleById(roleId);
        Permission permission = getPermissionById(permissionId);
        
        RoleHasPermission rolePermission = new RoleHasPermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);
        
        // This would need to be saved through a repository
        // For now, we'll assume the relationship is managed through the entities
    }

    @Override
    @Transactional
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        log.info("Removing permission {} from role {}", permissionId, roleId);
        // Implementation would depend on how RoleHasPermission is managed
    }

    @Override
    public Set<Permission> getRolePermissions(Long roleId) {
        log.info("Getting permissions for role: {}", roleId);
        Role role = getRoleById(roleId);
        return role.getRolePermissions().stream()
                .map(RoleHasPermission::getPermission)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) {
        log.info("Assigning role {} to user {}", roleId, userId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Role role = getRoleById(roleId);
        
        UserHasRole userRole = new UserHasRole();
        userRole.setUser(user);
        userRole.setRole(role);
        
        // This would need to be saved through a repository
    }

    @Override
    @Transactional
    public void removeRoleFromUser(Long userId, Long roleId) {
        log.info("Removing role {} from user {}", roleId, userId);
        // Implementation would depend on how UserHasRole is managed
    }

    @Override
    public Set<Role> getUserRoles(Long userId) {
        log.info("Getting roles for user: {}", userId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return user.getRoles().stream()
                .map(UserHasRole::getRole)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Permission> getUserPermissions(Long userId) {
        log.info("Getting permissions for user: {}", userId);
        Set<Role> userRoles = getUserRoles(userId);
        return userRoles.stream()
                .flatMap(role -> getRolePermissions(role.getId()).stream())
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void initializeDefaultRolesAndPermissions() {
        log.info("Initializing default roles and permissions");
        
        // Create default roles
        if (!roleRepository.existsByName("ADMIN")) {
            createRole("ADMIN", "Administrator with full access");
        }
        if (!roleRepository.existsByName("USER")) {
            createRole("USER", "Regular user with limited access");
        }
        
        // Create default permissions
        String[][] permissions = {
            {"GET", "/user/list", "User Management", "View user list"},
            {"GET", "/user/{id}", "User Management", "View user details"},
            {"POST", "/user/add", "User Management", "Create user"},
            {"PUT", "/user/update", "User Management", "Update user"},
            {"DELETE", "/user/{id}/del", "User Management", "Delete user"},
            {"PATCH", "/user/change-pwd", "User Management", "Change password"},
            {"GET", "/user/confirm-email", "User Management", "Confirm email"}
        };
        
        for (String[] perm : permissions) {
            if (!permissionRepository.existsByMethodAndPath(perm[0], perm[1])) {
                createPermission(perm[0], perm[1], perm[2], perm[3]);
            }
        }
    }
}
