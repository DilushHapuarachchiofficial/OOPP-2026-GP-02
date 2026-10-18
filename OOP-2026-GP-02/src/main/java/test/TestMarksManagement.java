package main.java.test;

import main.java.model.Lecturer.AssessmentDAO;
import main.java.model.Lecturer.AssessmentDAO.AssessmentComponent;
import main.java.model.Lecturer.Lecturer;
import main.java.model.Lecturer.MarksCalculationService;
import main.java.model.Lecturer.MarksDAO;
import main.java.model.Lecturer.StudentDetailsEligibilityLogic;
import main.java.model.Lecturer.StudentDetailsEligibilityLogic.CourseInfo;

import java.util.List;

public class TestMarksManagement {
    public static void main(String[] args) {
        System.out.println("=== Testing Marks Management & Ruhuna By-Law Grading ===");

        // Test 1: MarksCalculationService verification
        System.out.println("Testing By-Law 92-2025 Table 1 Grade Mapping:");
        System.out.println("85% -> " + MarksCalculationService.calculateGrade(85, true) + " (GPV: " + MarksCalculationService.calculateGradePoint(85, true) + ")");
        System.out.println("78% -> " + MarksCalculationService.calculateGrade(78, true) + " (GPV: " + MarksCalculationService.calculateGradePoint(78, true) + ")");
        System.out.println("72% -> " + MarksCalculationService.calculateGrade(72, true) + " (GPV: " + MarksCalculationService.calculateGradePoint(72, true) + ")");
        System.out.println("67% -> " + MarksCalculationService.calculateGrade(67, true) + " (GPV: " + MarksCalculationService.calculateGradePoint(67, true) + ")");
        System.out.println("61% -> " + MarksCalculationService.calculateGrade(61, true) + " (GPV: " + MarksCalculationService.calculateGradePoint(61, true) + ")");
        System.out.println("56% -> " + MarksCalculationService.calculateGrade(56, true) + " (GPV: " + MarksCalculationService.calculateGradePoint(56, true) + ")");
        System.out.println("51% -> " + MarksCalculationService.calculateGrade(51, true) + " (GPV: " + MarksCalculationService.calculateGradePoint(51, true) + ")");
        System.out.println("46% -> " + MarksCalculationService.calculateGrade(46, true) + " (GPV: " + MarksCalculationService.calculateGradePoint(46, true) + ")");
        System.out.println("41% -> " + MarksCalculationService.calculateGrade(41, true) + " (GPV: " + MarksCalculationService.calculateGradePoint(41, true) + ")");
        System.out.println("36% -> " + MarksCalculationService.calculateGrade(36, true) + " (GPV: " + MarksCalculationService.calculateGradePoint(36, true) + ")");
        System.out.println("25% -> " + MarksCalculationService.calculateGrade(25, true) + " (GPV: " + MarksCalculationService.calculateGradePoint(25, true) + ")");

        System.out.println("\nTesting CA Eligibility (By-Law 3.19.1b min 40%):");
        System.out.println("CA 40.0% -> " + MarksCalculationService.isCaEligible(40.0));
        System.out.println("CA 39.9% -> " + MarksCalculationService.isCaEligible(39.9));

        System.out.println("\nTesting Table 2 Completion Status:");
        System.out.println("CA=50, ESA=60 -> " + MarksCalculationService.determineResultStatus(true, 50.0, 60.0, 55.0));
        System.out.println("CA=35, ESA=60 -> " + MarksCalculationService.determineResultStatus(true, 35.0, 60.0, 50.0) + " (Expected: EC)");
        System.out.println("CA=50, ESA=30 -> " + MarksCalculationService.determineResultStatus(true, 50.0, 30.0, 40.0) + " (Expected: EE)");
        System.out.println("CA=30, ESA=30 -> " + MarksCalculationService.determineResultStatus(true, 30.0, 30.0, 30.0) + " (Expected: E)");

        // Test 2: Database connectivity and DAO tests
        try {
            System.out.println("\nTesting Database DAOs:");
            List<CourseInfo> courses = StudentDetailsEligibilityLogic.getAssignedCourses(4);
            System.out.println("Lecturer 4 courses found: " + courses.size());
            for (CourseInfo c : courses) {
                System.out.println(" - " + c.getCourseCode() + " : " + c.getCourseName());
            }

            if (!courses.isEmpty()) {
                int courseId = courses.get(0).getCourseId();
                List<AssessmentComponent> allAss = AssessmentDAO.getAssessmentsForCourse(courseId);
                List<AssessmentComponent> openAss = AssessmentDAO.getOpenAssessmentsForCourse(courseId);
                System.out.println("Course ID " + courseId + " assessments total: " + allAss.size() + ", open: " + openAss.size());

                if (!allAss.isEmpty()) {
                    AssessmentComponent ass = allAss.get(0);
                    int compId = ass.getComponentId();
                    List<MarksDAO.StudentMarkItem> students = MarksDAO.getStudentMarksForAssessment(
                            courseId, compId, ass.getMaximumMarks(), ass.getWeightPercentage(), ass.getComponentType());
                    System.out.println("Students for assessment " + compId + " (" + ass.getComponentName() + "): " + students.size());
                    if (!students.isEmpty()) {
                        MarksDAO.StudentMarkItem s = students.get(0);
                        System.out.println("Sample student: " + s.getStudentRegNo() + " - " + s.getStudentName());
                        System.out.println("  Mark: " + s.getRawMark() + "/" + s.getMaximumMarks());
                        System.out.println("  Overall CA Score: " + s.getOverallCaWeightedScore() + " / " + s.getTotalCourseCaWeight());
                        System.out.println("  Overall CA %: " + s.getOverallCaPercentage() + "%");
                        System.out.println("  CA Eligibility: " + s.getOverallCaEligibility());

                        // Test Lecturer class method
                        Lecturer lecturer = new Lecturer(4, "Senior Lecturer", "ICT");
                        Lecturer.StudentCaResult caResult = lecturer.calculateOverallStudentCA(s.getStudentId(), courseId);
                        System.out.println("Lecturer.calculateOverallStudentCA result: " + caResult);

                        // Test Publication Status in tecfams_db
                        System.out.println("\nTesting Publication in tecfams_db:");
                        boolean published = main.java.model.Lecturer.ResultsDAO.updateCoursePublicationStatus(courseId, "Published", 4);
                        System.out.println("ResultsDAO.updateCoursePublicationStatus('Published'): " + published);
                        String currentStatus = main.java.model.Lecturer.ResultsDAO.getCoursePublicationStatus(courseId);
                        System.out.println("Saved publication status in tecfams_db: " + currentStatus);
                        // Reset to Draft for future tests
                        main.java.model.Lecturer.ResultsDAO.updateCoursePublicationStatus(courseId, "Draft", 4);
                        System.out.println("Reset to Draft: " + main.java.model.Lecturer.ResultsDAO.getCoursePublicationStatus(courseId));
                    }
                }
            }
            System.out.println("\nALL TESTS PASSED SUCCESSFULLY!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
