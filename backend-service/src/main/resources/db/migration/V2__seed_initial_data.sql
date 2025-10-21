-- Seed Initial Data
-- Version: 2.0
-- Description: Insert default roles and permissions

-- ===================================
-- Insert Default Roles
-- ===================================
INSERT INTO tbl_role (name, description, created_at, updated_at) 
VALUES 
    ('ADMIN', 'Administrator with full access', NOW(), NOW()),
    ('USER', 'Regular user with limited access', NOW(), NOW())
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- ===================================
-- Insert Default Permissions
-- ===================================
INSERT INTO tbl_permission (method, path, category, description, created_at, updated_at) 
VALUES 
    -- User Management Permissions
    ('GET', '/user/list', 'User Management', 'View all users', NOW(), NOW()),
    ('GET', '/user/{id}', 'User Management', 'View user details', NOW(), NOW()),
    ('POST', '/user/add', 'User Management', 'Create new user', NOW(), NOW()),
    ('PUT', '/user/update', 'User Management', 'Update user information', NOW(), NOW()),
    ('DELETE', '/user/{id}/del', 'User Management', 'Delete user', NOW(), NOW()),
    ('PATCH', '/user/change-pwd', 'User Management', 'Change password', NOW(), NOW()),
    
    -- Role Management Permissions
    ('GET', '/role/list', 'Role Management', 'View all roles', NOW(), NOW()),
    ('POST', '/role/add', 'Role Management', 'Create new role', NOW(), NOW()),
    ('PUT', '/role/update', 'Role Management', 'Update role', NOW(), NOW()),
    ('DELETE', '/role/{id}/del', 'Role Management', 'Delete role', NOW(), NOW()),
    
    -- Authentication Permissions
    ('POST', '/auth/access-token', 'Authentication', 'Login', NOW(), NOW()),
    ('POST', '/auth/refresh-token', 'Authentication', 'Refresh token', NOW(), NOW())
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- ===================================
-- Assign Permissions to ADMIN Role
-- ===================================
INSERT INTO tbl_role_has_permission (role_id, permission_id, created_at, updated_at)
SELECT 
    r.id,
    p.id,
    NOW(),
    NOW()
FROM tbl_role r
CROSS JOIN tbl_permission p
WHERE r.name = 'ADMIN'
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- ===================================
-- Assign Limited Permissions to USER Role
-- ===================================
INSERT INTO tbl_role_has_permission (role_id, permission_id, created_at, updated_at)
SELECT 
    r.id,
    p.id,
    NOW(),
    NOW()
FROM tbl_role r
CROSS JOIN tbl_permission p
WHERE r.name = 'USER'
  AND p.path IN ('/user/list', '/user/{id}', '/user/update', '/user/change-pwd', '/auth/access-token', '/auth/refresh-token')
ON DUPLICATE KEY UPDATE updated_at = NOW();

