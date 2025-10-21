package vn.thanhquan.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.thanhquan.model.Permission;
import vn.thanhquan.model.Role;
import vn.thanhquan.service.RoleService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/role")
@Tag(name = "Role Management")
@Slf4j(topic = "ROLE-CONTROLLER")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Get all roles", description = "Get list of all roles")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> getAllRoles() {
        log.info("Getting all roles");
        List<Role> roles = roleService.getAllRoles();
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Roles retrieved successfully");
        result.put("data", roles);
        return result;
    }

    @Operation(summary = "Get role by ID", description = "Get role details by ID")
    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> getRoleById(@PathVariable Long roleId) {
        log.info("Getting role by id: {}", roleId);
        Role role = roleService.getRoleById(roleId);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Role retrieved successfully");
        result.put("data", role);
        return result;
    }

    @Operation(summary = "Create role", description = "Create a new role")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> createRole(@RequestBody Map<String, String> request) {
        log.info("Creating role: {}", request.get("name"));
        String name = request.get("name");
        String description = request.get("description");
        
        Role role = roleService.createRole(name, description);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "Role created successfully");
        result.put("data", role);
        
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Update role", description = "Update an existing role")
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> updateRole(@PathVariable Long roleId, @RequestBody Map<String, String> request) {
        log.info("Updating role: {}", roleId);
        String name = request.get("name");
        String description = request.get("description");
        
        Role role = roleService.updateRole(roleId, name, description);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Role updated successfully");
        result.put("data", role);
        return result;
    }

    @Operation(summary = "Delete role", description = "Delete a role")
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> deleteRole(@PathVariable Long roleId) {
        log.info("Deleting role: {}", roleId);
        roleService.deleteRole(roleId);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Role deleted successfully");
        result.put("data", "");
        return result;
    }

    @Operation(summary = "Get all permissions", description = "Get list of all permissions")
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> getAllPermissions() {
        log.info("Getting all permissions");
        List<Permission> permissions = roleService.getAllPermissions();
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Permissions retrieved successfully");
        result.put("data", permissions);
        return result;
    }

    @Operation(summary = "Assign permission to role", description = "Assign a permission to a role")
    @PostMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> assignPermissionToRole(@PathVariable Long roleId, @PathVariable Long permissionId) {
        log.info("Assigning permission {} to role {}", permissionId, roleId);
        roleService.assignPermissionToRole(roleId, permissionId);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Permission assigned to role successfully");
        result.put("data", "");
        return result;
    }

    @Operation(summary = "Get role permissions", description = "Get all permissions for a role")
    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> getRolePermissions(@PathVariable Long roleId) {
        log.info("Getting permissions for role: {}", roleId);
        Set<Permission> permissions = roleService.getRolePermissions(roleId);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Role permissions retrieved successfully");
        result.put("data", permissions);
        return result;
    }

    @Operation(summary = "Assign role to user", description = "Assign a role to a user")
    @PostMapping("/assign/{userId}/{roleId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> assignRoleToUser(@PathVariable Long userId, @PathVariable Long roleId) {
        log.info("Assigning role {} to user {}", roleId, userId);
        roleService.assignRoleToUser(userId, roleId);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Role assigned to user successfully");
        result.put("data", "");
        return result;
    }

    @Operation(summary = "Get user roles", description = "Get all roles for a user")
    @GetMapping("/user/{userId}/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> getUserRoles(@PathVariable Long userId) {
        log.info("Getting roles for user: {}", userId);
        Set<Role> roles = roleService.getUserRoles(userId);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "User roles retrieved successfully");
        result.put("data", roles);
        return result;
    }

    @Operation(summary = "Initialize default roles and permissions", description = "Initialize system with default roles and permissions")
    @PostMapping("/initialize")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> initializeDefaultRolesAndPermissions() {
        log.info("Initializing default roles and permissions");
        roleService.initializeDefaultRolesAndPermissions();
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Default roles and permissions initialized successfully");
        result.put("data", "");
        return result;
    }
}
