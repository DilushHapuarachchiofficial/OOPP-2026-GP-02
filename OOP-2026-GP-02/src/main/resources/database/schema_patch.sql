-- =========================================================================
-- TecFAMS Database Schema Patch for Admin Dashboard & Reports
-- Run this script in MySQL if you re-import a clean dump of tecfams_db
-- Note: DatabaseConnection.java also auto-applies these checks automatically.
-- =========================================================================

USE `tecfams_db`;

-- 1. Add address to admins table for admin profile management
ALTER TABLE `admins` ADD COLUMN IF NOT EXISTS `address` TEXT DEFAULT NULL;

-- 2. Add status to users table for account status tracking (Active / Deactivated)
ALTER TABLE `users` ADD COLUMN IF NOT EXISTS `status` VARCHAR(20) NOT NULL DEFAULT 'Active';

-- 3. Add attachment_path to notices table for notice attachments
ALTER TABLE `notices` ADD COLUMN IF NOT EXISTS `attachment_path` VARCHAR(255) DEFAULT NULL;

-- 4. Add file_path to course_materials table and ensure binary fields are optional
ALTER TABLE `course_materials` ADD COLUMN IF NOT EXISTS `file_path` VARCHAR(255) DEFAULT NULL;
ALTER TABLE `course_materials` MODIFY COLUMN `file_name` VARCHAR(255) NULL;
ALTER TABLE `course_materials` MODIFY COLUMN `file_data` LONGBLOB NULL;
