-- TecFAMS MySQL Script: Insert Year 1 Courses, Evaluation Components (Quiz, Assignment, Mid, Final),
-- Registrations and Marks for Student TG/2024/2085 (and peers).
-- Database: tecfams_db

USE tecfams_db;

-- 1. Insert Year 1 Semester 1 and Semester 2 Courses
INSERT IGNORE INTO courses (course_id, department_id, course_code, course_name, credit, course_type, semester, academic_year) VALUES
(12, 1, 'ICT1113', 'Fundamentals of Information Technology', 3, 'Theory_and_Practical', 1, 1),
(13, 1, 'ICT1123', 'Structured Programming', 3, 'Theory_and_Practical', 1, 1),
(14, 1, 'ICT1132', 'Database Management Systems I', 2, 'Theory_and_Practical', 1, 1),
(15, 4, 'ENG1112', 'English I', 2, 'Theory_Only', 1, 1),
(16, 4, 'TMS1113', 'Mathematics for Technology', 3, 'Theory_Only', 1, 1),
(17, 4, 'TCS1112', 'Essential Skills for Technology', 2, 'Theory_and_Practical', 1, 1),
(18, 1, 'ICT1212', 'Web Technologies', 2, 'Theory_and_Practical', 2, 1),
(19, 1, 'ICT1222', 'Operating Systems', 2, 'Theory_Only', 2, 1),
(20, 1, 'ICT1232', 'Data Communication and Networks', 2, 'Theory_Only', 2, 1),
(21, 4, 'ENG1212', 'English II', 2, 'Theory_Only', 2, 1);

-- 2. Update view_student_course_grades to include academic_year and semester
CREATE OR REPLACE VIEW view_student_course_grades AS
WITH finalcoursescores AS (
    SELECT m.student_id AS student_id, ec.course_id AS course_id,
    ROUND(SUM(((m.marks_obtained / ec.maximum_marks) * ec.weight_percentage)), 2) AS total_weighted_mark 
    FROM marks m 
    JOIN evaluation_components ec ON m.component_id = ec.component_id 
    GROUP BY m.student_id, ec.course_id
) 
SELECT s.student_id AS student_id, s.student_reg_no AS student_reg_no, s.full_name AS full_name,
c.course_id AS course_id, c.course_code AS course_code, c.course_name AS course_name, c.credit AS credit,
c.academic_year AS academic_year, c.semester AS semester,
fcs.total_weighted_mark AS final_mark,
(CASE WHEN (fcs.total_weighted_mark >= 85) THEN 'A+'
      WHEN (fcs.total_weighted_mark >= 80) THEN 'A'
      WHEN (fcs.total_weighted_mark >= 75) THEN 'A-'
      WHEN (fcs.total_weighted_mark >= 70) THEN 'B+'
      WHEN (fcs.total_weighted_mark >= 65) THEN 'B'
      WHEN (fcs.total_weighted_mark >= 60) THEN 'B-'
      WHEN (fcs.total_weighted_mark >= 55) THEN 'C+'
      WHEN (fcs.total_weighted_mark >= 50) THEN 'C'
      WHEN (fcs.total_weighted_mark >= 45) THEN 'C-'
      WHEN (fcs.total_weighted_mark >= 40) THEN 'D+'
      WHEN (fcs.total_weighted_mark >= 35) THEN 'D'
      ELSE 'E' END) AS grade,
(CASE WHEN (fcs.total_weighted_mark >= 80) THEN 4.00
      WHEN (fcs.total_weighted_mark >= 75) THEN 3.70
      WHEN (fcs.total_weighted_mark >= 70) THEN 3.30
      WHEN (fcs.total_weighted_mark >= 65) THEN 3.00
      WHEN (fcs.total_weighted_mark >= 60) THEN 2.70
      WHEN (fcs.total_weighted_mark >= 55) THEN 2.30
      WHEN (fcs.total_weighted_mark >= 50) THEN 2.00
      WHEN (fcs.total_weighted_mark >= 45) THEN 1.70
      WHEN (fcs.total_weighted_mark >= 40) THEN 1.30
      WHEN (fcs.total_weighted_mark >= 35) THEN 1.00
      ELSE 0.00 END) AS grade_point
FROM students s 
JOIN course_registrations cr ON s.student_id = cr.student_id 
JOIN courses c ON cr.course_id = c.course_id 
LEFT JOIN finalcoursescores fcs ON fcs.student_id = s.student_id AND fcs.course_id = c.course_id;

-- 3. Register students for Year 1 Courses
-- Students: 18 (Kumarasinghe), 19 (Smarathunga), 20 (Hapuarachchi HKDB), 21 (Hapuarachchi HDWB - TG/2024/2085)
INSERT IGNORE INTO course_registrations (student_id, course_id, semester, academic_year, registration_status) VALUES
-- Student 21 (TG/2024/2085) - Year 1 Sem 1
(21, 12, 1, 1, 'Registered'),
(21, 13, 1, 1, 'Registered'),
(21, 14, 1, 1, 'Registered'),
(21, 15, 1, 1, 'Registered'),
(21, 16, 1, 1, 'Registered'),
(21, 17, 1, 1, 'Registered'),
-- Student 21 (TG/2024/2085) - Year 1 Sem 2
(21, 10, 2, 1, 'Registered'),
(21, 11, 2, 1, 'Registered'),
(21, 18, 2, 1, 'Registered'),
(21, 19, 2, 1, 'Registered'),
(21, 20, 2, 1, 'Registered'),
(21, 21, 2, 1, 'Registered'),

-- Student 18 - Year 1 Sem 1 & 2
(18, 12, 1, 1, 'Registered'),
(18, 13, 1, 1, 'Registered'),
(18, 14, 1, 1, 'Registered'),
(18, 15, 1, 1, 'Registered'),
(18, 16, 1, 1, 'Registered'),
(18, 17, 1, 1, 'Registered'),
(18, 10, 2, 1, 'Registered'),
(18, 11, 2, 1, 'Registered'),
(18, 18, 2, 1, 'Registered'),
(18, 19, 2, 1, 'Registered'),
(18, 20, 2, 1, 'Registered'),
(18, 21, 2, 1, 'Registered');

-- 4. Insert Evaluation Components (Quiz, Assignment, Mid, Final) for Courses
-- Course 12 (ICT1113)
INSERT IGNORE INTO evaluation_components (component_id, course_id, component_name, component_type, maximum_marks, weight_percentage) VALUES
(10, 12, 'Quiz 01', 'CA', 100.00, 10.00),
(11, 12, 'Assignment 01', 'CA', 100.00, 15.00),
(12, 12, 'Mid-Semester Exam', 'CA', 100.00, 15.00),
(13, 12, 'Final Examination', 'Final', 100.00, 60.00),

-- Course 13 (ICT1123)
(14, 13, 'Quiz 01 (Coding)', 'CA', 100.00, 10.00),
(15, 13, 'Programming Assignment', 'CA', 100.00, 15.00),
(16, 13, 'Mid-Semester Exam', 'CA', 100.00, 15.00),
(17, 13, 'Final Theory & Practical', 'Final', 100.00, 60.00),

-- Course 14 (ICT1132)
(18, 14, 'Quiz 01 (SQL)', 'CA', 100.00, 10.00),
(19, 14, 'Database Mini Project', 'CA', 100.00, 15.00),
(20, 14, 'Mid-Semester Exam', 'CA', 100.00, 15.00),
(21, 14, 'Final Examination', 'Final', 100.00, 60.00),

-- Course 15 (ENG1112)
(22, 15, 'Grammar & Vocab Quiz', 'CA', 100.00, 10.00),
(23, 15, 'Writing Assignment', 'CA', 100.00, 15.00),
(24, 15, 'Mid-Semester Speaking Test', 'CA', 100.00, 15.00),
(25, 15, 'Final Examination', 'Final', 100.00, 60.00),

-- Course 16 (TMS1113)
(26, 16, 'Quiz 01', 'CA', 100.00, 10.00),
(27, 16, 'Assignment 01', 'CA', 100.00, 15.00),
(28, 16, 'Mid-Semester Exam', 'CA', 100.00, 15.00),
(29, 16, 'Final Examination', 'Final', 100.00, 60.00),

-- Course 17 (TCS1112)
(30, 17, 'Quiz 01', 'CA', 100.00, 10.00),
(31, 17, 'Group Presentation', 'CA', 100.00, 15.00),
(32, 17, 'Mid-Semester Evaluation', 'CA', 100.00, 15.00),
(33, 17, 'Final Examination', 'Final', 100.00, 60.00),

-- Course 10 (TCS1212)
(34, 10, 'Quiz 01', 'CA', 100.00, 10.00),
(35, 10, 'Case Study Assignment', 'CA', 100.00, 15.00),
(36, 10, 'Mid-Semester Exam', 'CA', 100.00, 15.00),
(37, 10, 'Final Examination', 'Final', 100.00, 60.00),

-- Course 11 (TMS1233)
(38, 11, 'Quiz 01', 'CA', 100.00, 10.00),
(39, 11, 'Assignment 01', 'CA', 100.00, 15.00),
(40, 11, 'Mid-Semester Exam', 'CA', 100.00, 15.00),
(41, 11, 'Final Examination', 'Final', 100.00, 60.00),

-- Course 18 (ICT1212)
(42, 18, 'Quiz 01 (HTML/CSS)', 'CA', 100.00, 10.00),
(43, 18, 'Web Project Assignment', 'CA', 100.00, 15.00),
(44, 18, 'Mid-Semester Exam', 'CA', 100.00, 15.00),
(45, 18, 'Final Practical Exam', 'Final', 100.00, 60.00),

-- Course 19 (ICT1222)
(46, 19, 'Quiz 01', 'CA', 100.00, 10.00),
(47, 19, 'OS Lab Assignment', 'CA', 100.00, 15.00),
(48, 19, 'Mid-Semester Exam', 'CA', 100.00, 15.00),
(49, 19, 'Final Examination', 'Final', 100.00, 60.00),

-- Course 20 (ICT1232)
(50, 20, 'Quiz 01', 'CA', 100.00, 10.00),
(51, 20, 'Network Packet Assignment', 'CA', 100.00, 15.00),
(52, 20, 'Mid-Semester Exam', 'CA', 100.00, 15.00),
(53, 20, 'Final Examination', 'Final', 100.00, 60.00),

-- Course 21 (ENG1212)
(54, 21, 'Quiz 01', 'CA', 100.00, 10.00),
(55, 21, 'Report Writing Assignment', 'CA', 100.00, 15.00),
(56, 21, 'Mid-Semester Presentation', 'CA', 100.00, 15.00),
(57, 21, 'Final Examination', 'Final', 100.00, 60.00),

-- Course 2 (ICT2113 - Data Structures) in Year 2 Sem 1
(58, 2, 'Quiz 01 (Algorithm Analysis)', 'CA', 100.00, 10.00),
(59, 2, 'DSA Programming Assignment', 'CA', 100.00, 15.00),
(60, 2, 'Mid-Semester Exam', 'CA', 100.00, 15.00),
(61, 2, 'Final Practical & Theory Exam', 'Final', 100.00, 60.00),

-- Course 4 (ENG2122 - English III) in Year 2 Sem 1
(62, 4, 'Quiz 01', 'CA', 100.00, 10.00),
(63, 4, 'Technical Writing Assignment', 'CA', 100.00, 15.00),
(64, 4, 'Mid-Semester Exam', 'CA', 100.00, 15.00),
(65, 4, 'Final Examination', 'Final', 100.00, 60.00),

-- Course 5 (ICT2142 - E-Business) in Year 2 Sem 1
(66, 5, 'Quiz 01', 'CA', 100.00, 10.00),
(67, 5, 'E-Commerce Model Assignment', 'CA', 100.00, 15.00),
(68, 5, 'Mid-Semester Exam', 'CA', 100.00, 15.00),
(69, 5, 'Final Examination', 'Final', 100.00, 60.00);


-- 5. Insert Marks for Student 21 (TG/2024/2085)
-- Year 1 Semester 1:
-- ICT1113 (Comp 10,11,12,13) -> Q: 88, Ass: 90, Mid: 84, Final: 86 => Total: 8.8 + 13.5 + 12.6 + 51.6 = 86.5 (A+)
-- ICT1123 (Comp 14,15,16,17) -> Q: 92, Ass: 95, Mid: 86, Final: 88 => Total: 9.2 + 14.25 + 12.9 + 52.8 = 89.15 (A+)
-- ICT1132 (Comp 18,19,20,21) -> Q: 85, Ass: 88, Mid: 82, Final: 80 => Total: 8.5 + 13.2 + 12.3 + 48.0 = 82.0 (A)
-- ENG1112 (Comp 22,23,24,25) -> Q: 80, Ass: 85, Mid: 78, Final: 76 => Total: 8.0 + 12.75 + 11.7 + 45.6 = 78.05 (A-)
-- TMS1113 (Comp 26,27,28,29) -> Q: 90, Ass: 85, Mid: 80, Final: 82 => Total: 9.0 + 12.75 + 12.0 + 49.2 = 82.95 (A)
-- TCS1112 (Comp 30,31,32,33) -> Q: 95, Ass: 92, Mid: 90, Final: 88 => Total: 9.5 + 13.8 + 13.5 + 52.8 = 89.6 (A+)

-- Year 1 Semester 2:
-- TCS1212 (Comp 34,35,36,37) -> Q: 85, Ass: 88, Mid: 80, Final: 82 => Total: 8.5 + 13.2 + 12.0 + 49.2 = 82.9 (A)
-- TMS1233 (Comp 38,39,40,41) -> Q: 90, Ass: 92, Mid: 88, Final: 85 => Total: 9.0 + 13.8 + 13.2 + 51.0 = 87.0 (A+)
-- ICT1212 (Comp 42,43,44,45) -> Q: 94, Ass: 96, Mid: 90, Final: 92 => Total: 9.4 + 14.4 + 13.5 + 55.2 = 92.5 (A+)
-- ICT1222 (Comp 46,47,48,49) -> Q: 82, Ass: 85, Mid: 78, Final: 80 => Total: 8.2 + 12.75 + 11.7 + 48.0 = 80.65 (A)
-- ICT1232 (Comp 50,51,52,53) -> Q: 88, Ass: 86, Mid: 84, Final: 85 => Total: 8.8 + 12.9 + 12.6 + 51.0 = 85.3 (A+)
-- ENG1212 (Comp 54,55,56,57) -> Q: 84, Ass: 86, Mid: 80, Final: 82 => Total: 8.4 + 12.9 + 12.0 + 49.2 = 82.5 (A)

-- Year 2 Semester 1:
-- ICT2113 (Comp 58,59,60,61) -> Q: 90, Ass: 92, Mid: 85, Final: 88 => Total: 9.0 + 13.8 + 12.75 + 52.8 = 88.35 (A+)
-- ENG2122 (Comp 62,63,64,65) -> Q: 85, Ass: 88, Mid: 82, Final: 80 => Total: 8.5 + 13.2 + 12.3 + 48.0 = 82.0 (A)
-- ICT2142 (Comp 66,67,68,69) -> Q: 88, Ass: 90, Mid: 85, Final: 86 => Total: 8.8 + 13.5 + 12.75 + 51.6 = 86.65 (A+)

INSERT IGNORE INTO marks (component_id, student_id, marks_obtained, uploaded_by) VALUES
-- Student 21 Year 1 Sem 1
(10, 21, 88.00, 4), (11, 21, 90.00, 4), (12, 21, 84.00, 4), (13, 21, 86.00, 4),
(14, 21, 92.00, 4), (15, 21, 95.00, 4), (16, 21, 86.00, 4), (17, 21, 88.00, 4),
(18, 21, 85.00, 6), (19, 21, 88.00, 6), (20, 21, 82.00, 6), (21, 21, 80.00, 6),
(22, 21, 80.00, 11), (23, 21, 85.00, 11), (24, 21, 78.00, 11), (25, 21, 76.00, 11),
(26, 21, 90.00, 11), (27, 21, 85.00, 11), (28, 21, 80.00, 11), (29, 21, 82.00, 11),
(30, 21, 95.00, 11), (31, 21, 92.00, 11), (32, 21, 90.00, 11), (33, 21, 88.00, 11),

-- Student 21 Year 1 Sem 2
(34, 21, 85.00, 11), (35, 21, 88.00, 11), (36, 21, 80.00, 11), (37, 21, 82.00, 11),
(38, 21, 90.00, 11), (39, 21, 92.00, 11), (40, 21, 88.00, 11), (41, 21, 85.00, 11),
(42, 21, 94.00, 4), (43, 21, 96.00, 4), (44, 21, 90.00, 4), (45, 21, 92.00, 4),
(46, 21, 82.00, 6), (47, 21, 85.00, 6), (48, 21, 78.00, 6), (49, 21, 80.00, 6),
(50, 21, 88.00, 6), (51, 21, 86.00, 6), (52, 21, 84.00, 6), (53, 21, 85.00, 6),
(54, 21, 84.00, 11), (55, 21, 86.00, 11), (56, 21, 80.00, 11), (57, 21, 82.00, 11),

-- Student 21 Year 2 Sem 1 Additional
(58, 21, 90.00, 4), (59, 21, 92.00, 4), (60, 21, 85.00, 4), (61, 21, 88.00, 4),
(62, 21, 85.00, 11), (63, 21, 88.00, 11), (64, 21, 82.00, 11), (65, 21, 80.00, 11),
(66, 21, 88.00, 4), (67, 21, 90.00, 4), (68, 21, 85.00, 4), (69, 21, 86.00, 4);

-- Also Insert for Student 18 for comparison
INSERT IGNORE INTO marks (component_id, student_id, marks_obtained, uploaded_by) VALUES
(10, 18, 80.00, 4), (11, 18, 85.00, 4), (12, 18, 78.00, 4), (13, 18, 80.00, 4),
(14, 18, 85.00, 4), (15, 18, 88.00, 4), (16, 18, 82.00, 4), (17, 18, 80.00, 4),
(18, 18, 78.00, 6), (19, 18, 82.00, 6), (20, 18, 75.00, 6), (21, 18, 76.00, 6),
(22, 18, 75.00, 11), (23, 18, 80.00, 11), (24, 18, 72.00, 11), (25, 18, 74.00, 11),
(26, 18, 85.00, 11), (27, 18, 80.00, 11), (28, 18, 76.00, 11), (29, 18, 78.00, 11),
(30, 18, 90.00, 11), (31, 18, 88.00, 11), (32, 18, 85.00, 11), (33, 18, 82.00, 11);
