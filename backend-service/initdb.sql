-- ===================================
-- Initial Database Setup Script
-- ===================================
-- This script is executed when MySQL container starts for the first time
-- It creates the application database

CREATE DATABASE IF NOT EXISTS `backend-service` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant privileges to root user (for development only)
GRANT ALL PRIVILEGES ON `backend-service`.* TO 'root'@'%';
FLUSH PRIVILEGES;

-- Switch to the new database
USE `backend-service`;

-- Database is ready
-- Flyway will handle schema migrations
SELECT 'Database backend-service created successfully' AS message;

