-- =========================================================================
-- TecFAMS - Lecturer Marks Management Database Migration Script
-- Faculty of Technology, University of Ruhuna
-- =========================================================================

USE `tecfams_db`;

-- 1. Extend evaluation_components with assessment_type, assessment_date, status
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'tecfams_db' AND TABLE_NAME = 'evaluation_components' AND COLUMN_NAME = 'assessment_type');

SET @stmt = IF(@col_exists = 0, 
    'ALTER TABLE `evaluation_components` ADD COLUMN `assessment_type` VARCHAR(30) NOT NULL DEFAULT \'General\' AFTER `component_type`', 
    'SELECT 1');
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists2 = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'tecfams_db' AND TABLE_NAME = 'evaluation_components' AND COLUMN_NAME = 'assessment_date');

SET @stmt2 = IF(@col_exists2 = 0, 
    'ALTER TABLE `evaluation_components` ADD COLUMN `assessment_date` DATE DEFAULT NULL AFTER `weight_percentage`', 
    'SELECT 1');
PREPARE stmt2 FROM @stmt2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

SET @col_exists3 = (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'tecfams_db' AND TABLE_NAME = 'evaluation_components' AND COLUMN_NAME = 'status');

SET @stmt3 = IF(@col_exists3 = 0, 
    'ALTER TABLE `evaluation_components` ADD COLUMN `status` ENUM(\'Draft\',\'Open\',\'Closed\',\'Published\') NOT NULL DEFAULT \'Open\' AFTER `assessment_date`', 
    'SELECT 1');
PREPARE stmt3 FROM @stmt3;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

-- 2. Update existing evaluation components with appropriate assessment types
UPDATE `evaluation_components` SET `assessment_type` = 'Quiz' WHERE `component_id` = 1;
UPDATE `evaluation_components` SET `assessment_type` = 'Midterm' WHERE `component_id` = 2;
UPDATE `evaluation_components` SET `assessment_type` = 'Mini-Project' WHERE `component_id` = 3;
UPDATE `evaluation_components` SET `assessment_type` = 'End Exam' WHERE `component_id` = 4;
UPDATE `evaluation_components` SET `assessment_type` = 'Continuous Assessment' WHERE `component_id` = 5;
UPDATE `evaluation_components` SET `assessment_type` = 'End Exam' WHERE `component_id` = 6;

-- 3. Create course_result_status table for tracking draft/reviewed/published lifecycle
CREATE TABLE IF NOT EXISTS `course_result_status` (
  `status_id` int(11) NOT NULL AUTO_INCREMENT,
  `course_id` int(11) NOT NULL,
  `status` enum('Draft','Reviewed','Published') NOT NULL DEFAULT 'Draft',
  `published_by` int(11) DEFAULT NULL,
  `published_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`status_id`),
  UNIQUE KEY `uq_course_result_status` (`course_id`),
  CONSTRAINT `fk_crs_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_crs_lecturer` FOREIGN KEY (`published_by`) REFERENCES `lecturers` (`lecturer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Initialize course_result_status for all existing courses
INSERT INTO `course_result_status` (`course_id`, `status`) VALUES
(1, 'Draft'),
(2, 'Draft'),
(3, 'Draft'),
(4, 'Draft')
ON DUPLICATE KEY UPDATE `status` = `status`;

-- 4. Create or Replace view_course_final_results
CREATE OR REPLACE VIEW `view_course_final_results` AS
WITH course_ca_totals AS (
    SELECT 
        course_id,
        COUNT(component_id) AS total_ca_components,
        COALESCE(SUM(weight_percentage), 0) AS total_ca_weight
    FROM evaluation_components
    WHERE component_type = 'CA'
    GROUP BY course_id
),
course_final_totals AS (
    SELECT 
        course_id,
        COUNT(component_id) AS total_final_components,
        COALESCE(SUM(weight_percentage), 0) AS total_final_weight
    FROM evaluation_components
    WHERE component_type = 'Final'
    GROUP BY course_id
),
student_ca_scores AS (
    SELECT 
        ec.course_id,
        m.student_id,
        COUNT(m.mark_id) AS entered_ca_count,
        SUM((m.marks_obtained / ec.maximum_marks) * ec.weight_percentage) AS weighted_ca_score
    FROM marks m
    JOIN evaluation_components ec ON m.component_id = ec.component_id
    WHERE ec.component_type = 'CA'
    GROUP BY ec.course_id, m.student_id
),
student_final_scores AS (
    SELECT 
        ec.course_id,
        m.student_id,
        COUNT(m.mark_id) AS entered_final_count,
        SUM((m.marks_obtained / ec.maximum_marks) * ec.weight_percentage) AS weighted_final_score
    FROM marks m
    JOIN evaluation_components ec ON m.component_id = ec.component_id
    WHERE ec.component_type = 'Final'
    GROUP BY ec.course_id, m.student_id
),
calculated_results AS (
    SELECT 
        s.student_id,
        s.student_reg_no,
        s.full_name,
        c.course_id,
        c.course_code,
        c.course_name,
        cr.academic_year,
        cr.semester,
        cr.registration_status,
        
        COALESCE(cct.total_ca_components, 0) AS total_ca_components,
        COALESCE(scs.entered_ca_count, 0) AS entered_ca_count,
        COALESCE(cft.total_final_components, 0) AS total_final_components,
        COALESCE(sfs.entered_final_count, 0) AS entered_final_count,
        
        COALESCE(cct.total_ca_weight, 0) AS total_ca_weight,
        COALESCE(cft.total_final_weight, 0) AS total_final_weight,
        
        ROUND(COALESCE(scs.weighted_ca_score, 0.00), 2) AS weighted_ca_score,
        ROUND(COALESCE(sfs.weighted_final_score, 0.00), 2) AS weighted_final_score,
        
        CASE 
            WHEN COALESCE(cct.total_ca_weight, 0) > 0 AND scs.entered_ca_count > 0 THEN
                ROUND((scs.weighted_ca_score / cct.total_ca_weight) * 100.0, 2)
            ELSE NULL 
        END AS ca_percentage,
        
        CASE 
            WHEN COALESCE(cft.total_final_weight, 0) > 0 AND sfs.entered_final_count > 0 THEN
                ROUND((sfs.weighted_final_score / cft.total_final_weight) * 100.0, 2)
            ELSE NULL 
        END AS end_exam_percentage,
        
        ROUND(COALESCE(scs.weighted_ca_score, 0.00) + COALESCE(sfs.weighted_final_score, 0.00), 2) AS final_calculated_marks,
        COALESCE(crs.status, 'Draft') AS publication_status
    FROM students s
    JOIN course_registrations cr ON s.student_id = cr.student_id
    JOIN courses c ON cr.course_id = c.course_id
    LEFT JOIN course_ca_totals cct ON c.course_id = cct.course_id
    LEFT JOIN course_final_totals cft ON c.course_id = cft.course_id
    LEFT JOIN student_ca_scores scs ON s.student_id = scs.student_id AND c.course_id = scs.course_id
    LEFT JOIN student_final_scores sfs ON s.student_id = sfs.student_id AND c.course_id = sfs.course_id
    LEFT JOIN course_result_status crs ON c.course_id = crs.course_id
    WHERE cr.registration_status IN ('Registered', 'Repeat')
)
SELECT 
    student_id,
    student_reg_no,
    full_name,
    course_id,
    course_code,
    course_name,
    academic_year,
    semester,
    registration_status,
    ca_percentage,
    CASE 
        WHEN ca_percentage IS NULL THEN 'PENDING'
        WHEN ca_percentage >= 40.00 THEN 'ELIGIBLE'
        ELSE 'NOT_ELIGIBLE'
    END AS ca_eligibility,
    end_exam_percentage,
    weighted_ca_score,
    weighted_final_score,
    final_calculated_marks,
    -- Faculty of Technology, University of Ruhuna Student Handbook By-Law No. 92-2025 Table 1 & Table 2
    CASE 
        WHEN entered_ca_count < total_ca_components OR (total_final_components > 0 AND entered_final_count < total_final_components) THEN 'I'
        WHEN final_calculated_marks >= 85.00 THEN 'A+'
        WHEN final_calculated_marks >= 75.00 THEN 'A'
        WHEN final_calculated_marks >= 70.00 THEN 'A-'
        WHEN final_calculated_marks >= 65.00 THEN 'B+'
        WHEN final_calculated_marks >= 60.00 THEN 'B'
        WHEN final_calculated_marks >= 55.00 THEN 'B-'
        WHEN final_calculated_marks >= 50.00 THEN 'C+'
        WHEN final_calculated_marks >= 45.00 THEN 'C'
        WHEN final_calculated_marks >= 40.00 THEN 'C-'
        WHEN final_calculated_marks >= 35.00 THEN 'D'
        ELSE 'E'
    END AS letter_grade,
    CASE 
        WHEN entered_ca_count < total_ca_components OR (total_final_components > 0 AND entered_final_count < total_final_components) THEN 0.00
        WHEN final_calculated_marks >= 85.00 THEN 4.00
        WHEN final_calculated_marks >= 75.00 THEN 4.00
        WHEN final_calculated_marks >= 70.00 THEN 3.70
        WHEN final_calculated_marks >= 65.00 THEN 3.30
        WHEN final_calculated_marks >= 60.00 THEN 3.00
        WHEN final_calculated_marks >= 55.00 THEN 2.70
        WHEN final_calculated_marks >= 50.00 THEN 2.30
        WHEN final_calculated_marks >= 45.00 THEN 2.00
        WHEN final_calculated_marks >= 40.00 THEN 1.70
        WHEN final_calculated_marks >= 35.00 THEN 1.30
        ELSE 0.00
    END AS grade_point,
    -- Result Status (Handbook Section 3.19.1 & Table 2)
    CASE 
        WHEN entered_ca_count < total_ca_components OR (total_final_components > 0 AND entered_final_count < total_final_components) THEN 'Incomplete'
        WHEN ca_percentage < 40.00 AND (total_final_components = 0 OR end_exam_percentage < 35.00) THEN 'E'
        WHEN ca_percentage < 40.00 THEN 'EC'
        WHEN total_final_components > 0 AND end_exam_percentage < 35.00 THEN 'EE'
        WHEN final_calculated_marks >= 45.00 THEN 'Pass'
        WHEN final_calculated_marks >= 35.00 THEN 'Pass (D/C-)'
        ELSE 'Fail'
    END AS result_status,
    publication_status
FROM calculated_results;
