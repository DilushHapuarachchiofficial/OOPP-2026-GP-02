-- TecFAMS MySQL Server 8.0 Database Insertion Script
-- Real Subjects for Level II Semester I (Student TG/2024/2085)
-- Database: tecfams_db

USE tecfams_db;

-- 1. Update ICT2122 Course Name
UPDATE courses SET course_name = 'Object Oriented Programming' WHERE course_code = 'ICT2122';

-- 2. Insert Remaining Real Courses if not already present
INSERT IGNORE INTO courses (course_id, department_id, course_code, course_name, credit, course_type, semester, academic_year) VALUES
(5, 1, 'ICT2142', 'E-Business Systems', 2, 'Theory_and_Practical', 1, 2),
(6, 1, 'ICT2152', 'Object Oriented Analysis and Design', 2, 'Theory_and_Practical', 1, 2),
(7, 1, 'ICT2162', 'Management Information Systems', 2, 'Theory_and_Practical', 1, 2),
(8, 4, 'TCS2112', 'Business Economics', 2, 'Theory_Only', 1, 2),
(9, 4, 'TCS2121', 'Soft Skills', 1, 'Theory_and_Practical', 1, 2),
(10, 4, 'TCS1212', 'Fundamentals of Management', 2, 'Theory_Only', 2, 1),
(11, 4, 'TMS1233', 'Discrete Mathematics', 3, 'Theory_Only', 2, 1);
