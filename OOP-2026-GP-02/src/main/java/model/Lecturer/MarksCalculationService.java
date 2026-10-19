package main.java.model.Lecturer;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MarksCalculationService {

    // Minimum Continuous Assessment (CA) percentage required for final exam eligibility & passing CA (Grade C- threshold, 40%)
    public static final double CA_ELIGIBILITY_THRESHOLD = 40.00;
    public static final double CA_PASS_THRESHOLD = 40.00;

    // Minimum End-of-Semester Assessment (ESA) percentage required to pass ESA (Grade D threshold, 35%)
    public static final double ESA_PASS_THRESHOLD = 35.00;

    // Minimum final course score required for passing grade D (35.00)
    public static final double PASS_MINIMUM_THRESHOLD = 35.00;

    // Benchmark final score for satisfactory performance (Grade C, 45.00)
    public static final double PASS_BENCHMARK_THRESHOLD = 45.00;

    /**
     * Calculates the normalized percentage score for an assessment out of 100.
     *
     * @param rawMark The mark obtained by the student.
     * @param maximumMark The maximum possible raw mark for this assessment.
     * @return Normalized percentage rounded to 2 decimal places, or null if rawMark is null.
     */
    public static Double calculatePercentage(Double rawMark, double maximumMark) {
        if (rawMark == null) {
            return null;
        }
        if (maximumMark <= 0.0) {
            throw new IllegalArgumentException("Maximum mark must be greater than zero.");
        }
        double pct = (rawMark / maximumMark) * 100.0;
        return round2(pct);
    }

    /**
     * Calculates the weighted contribution of an assessment towards the course total (100%).
     * Formula: (rawMark / maximumMark) * weightPercentage.
     *
     * @param rawMark The mark obtained by the student.
     * @param maximumMark The maximum possible raw mark for this assessment.
     * @param weightPercentage The percentage contribution towards the course (e.g. 10.0 for 10%).
     * @return Weighted contribution rounded to 2 decimal places, or 0.00 if rawMark is null.
     */
    public static double calculateWeightedContribution(Double rawMark, double maximumMark, double weightPercentage) {
        if (rawMark == null || maximumMark <= 0.0 || weightPercentage <= 0.0) {
            return 0.00;
        }
        double weighted = (rawMark / maximumMark) * weightPercentage;
        return round2(weighted);
    }

    /**
     * Calculates the overall Continuous Assessment (CA) mark out of 100%.
     * Formula: (Total CA Weighted Contribution / Total Configured CA Weight) * 100.
     *
     * @param totalCaWeightedScore Sum of weighted contributions from all CA assessments.
     * @param totalCaWeight Sum of configured weight percentages for all CA assessments (e.g. 40.0%).
     * @return CA percentage out of 100 rounded to 2 decimal places, or null if totalCaWeight is 0.
     */
    public static Double calculateCaPercentage(double totalCaWeightedScore, double totalCaWeight) {
        if (totalCaWeight <= 0.0) {
            return null;
        }
        double pct = (totalCaWeightedScore / totalCaWeight) * 100.0;
        return round2(pct);
    }

    /**
     * Determines Continuous Assessment (CA) eligibility for End-of-Semester Examination.
     * Handbook Section 3.19.1(b): "Percentage pass mark for the CA is equivalent to the minimum
     * mark assigned for Grade C- (40%)."
     *
     * @param caPercentage CA percentage out of 100.
     * @return "ELIGIBLE", "NOT_ELIGIBLE", or "PENDING" (if marks are missing/unentered).
     */
    public static String determineCaEligibility(Double caPercentage) {
        if (caPercentage == null) {
            return "PENDING";
        }
        BigDecimal bd = BigDecimal.valueOf(caPercentage).setScale(2, RoundingMode.HALF_UP);
        BigDecimal threshold = BigDecimal.valueOf(CA_ELIGIBILITY_THRESHOLD).setScale(2, RoundingMode.HALF_UP);
        return bd.compareTo(threshold) >= 0 ? "ELIGIBLE" : "NOT_ELIGIBLE";
    }

    /**
     * Determines whether a student is CA Eligible as a boolean.
     */
    public static boolean isCaEligible(Double caPercentage) {
        return "ELIGIBLE".equals(determineCaEligibility(caPercentage));
    }

    /**
     * Determines the University of Ruhuna Faculty of Technology Official Letter Grade.
     * Reference: Faculty Student Handbook (2026 / By-Law No. 92-2025, Section 3.19.2 Table 1).
     *
     * Marks Range | Grade | Grade Point Value (GPV)
     * 85 and above| A+    | 4.00
     * 75 to 84    | A     | 4.00
     * 70 to 74    | A-    | 3.70
     * 65 to 69    | B+    | 3.30
     * 60 to 64    | B     | 3.00
     * 55 to 59    | B-    | 2.70
     * 50 to 54    | C+    | 2.30
     * 45 to 49    | C     | 2.00
     * 40 to 44    | C-    | 1.70
     * 35 to 39    | D     | 1.30
     * 00 to 34    | E     | 0.00
     *
     * @param finalMark Total final course mark (out of 100).
     * @param isComplete Whether all required assessments for this course have marks entered.
     * @return Letter grade (e.g. "A+", "A", "B+", "C", "D", "E", or "I" for Incomplete).
     */
    public static String calculateGrade(double finalMark, boolean isComplete) {
        if (!isComplete) {
            return "I"; // Incomplete
        }
        double mark = round2(finalMark);
        if (mark >= 85.00) return "A+";
        if (mark >= 75.00) return "A";
        if (mark >= 70.00) return "A-";
        if (mark >= 65.00) return "B+";
        if (mark >= 60.00) return "B";
        if (mark >= 55.00) return "B-";
        if (mark >= 50.00) return "C+";
        if (mark >= 45.00) return "C";
        if (mark >= 40.00) return "C-";
        if (mark >= 35.00) return "D";
        return "E";
    }

    /**
     * Determines the University of Ruhuna Official Grade Point Value (GPV).
     * Reference: Faculty Student Handbook Table 1.
     *
     * @param finalMark Total final course mark (out of 100).
     * @param isComplete Whether all required assessments for this course have marks entered.
     * @return Grade point value (0.00 to 4.00).
     */
    public static double calculateGradePoint(double finalMark, boolean isComplete) {
        if (!isComplete) {
            return 0.00;
        }
        double mark = round2(finalMark);
        if (mark >= 85.00) return 4.00;
        if (mark >= 75.00) return 4.00;
        if (mark >= 70.00) return 3.70;
        if (mark >= 65.00) return 3.30;
        if (mark >= 60.00) return 3.00;
        if (mark >= 55.00) return 2.70;
        if (mark >= 50.00) return 2.30;
        if (mark >= 45.00) return 2.00;
        if (mark >= 40.00) return 1.70;
        if (mark >= 35.00) return 1.30;
        return 0.00;
    }

    /**
     * Determines the overall academic result status according to Handbook Section 3.19.1 & Table 2.
     *
     * Table 2 References:
     * - "E"  : Both CA and ESA marks are below the prescribed minimum.
     * - "EC" : Only CA mark is below the prescribed minimum (Incomplete CA).
     * - "EE" : Only ESA mark is below the prescribed minimum (Incomplete ESA).
     * - "Pass" : Passed both CA (>= 40%) and ESA (>= 35%) with total grade >= D (>= 35%).
     *
     * @param isComplete True if all required component marks are entered.
     * @param caPercentage The CA percentage out of 100.
     * @param esaPercentage The ESA (End Exam) percentage out of 100 (or null if no ESA configured/entered).
     * @param finalMark Total final course mark.
     * @return Result status description.
     */
    public static String determineResultStatus(boolean isComplete, Double caPercentage, Double esaPercentage, double finalMark) {
        if (!isComplete) {
            return "Incomplete";
        }
        boolean caFail = caPercentage != null && caPercentage < CA_PASS_THRESHOLD;
        boolean esaFail = esaPercentage != null && esaPercentage < ESA_PASS_THRESHOLD;

        if (caFail && esaFail) {
            return "E (CA & ESA Failed)";
        }
        if (caFail) {
            return "EC (Incomplete CA)";
        }
        if (esaFail) {
            return "EE (Incomplete ESA)";
        }

        double mark = round2(finalMark);
        if (mark >= PASS_BENCHMARK_THRESHOLD) {
            return "Pass";
        }
        if (mark >= PASS_MINIMUM_THRESHOLD) {
            return "Pass (Conditional)";
        }
        return "Fail";
    }

    /**
     * Overload for backwards compatibility when ESA percentage is not separately passed.
     */
    public static String determineResultStatus(boolean isComplete, Double caPercentage, double finalMark) {
        return determineResultStatus(isComplete, caPercentage, null, finalMark);
    }

    /**
     * Validates that an assessment raw mark is numeric, non-negative, and does not exceed maximum mark.
     *
     * @param markStr Input string from UI text field.
     * @param maxMark Maximum allowed mark for assessment.
     * @return ValidationResult containing parsed double or error message.
     */
    public static ValidationResult validateRawMark(String markStr, double maxMark) {
        if (markStr == null || markStr.trim().isEmpty()) {
            return new ValidationResult(true, null, null); // Unentered mark is valid blank
        }
        String clean = markStr.trim();
        try {
            double val = Double.parseDouble(clean);
            if (val < 0.0) {
                return new ValidationResult(false, null, "Mark cannot be negative (" + clean + ").");
            }
            if (val > maxMark) {
                return new ValidationResult(false, null, "Mark (" + clean + ") exceeds maximum allowed mark of " + maxMark + ".");
            }
            return new ValidationResult(true, round2(val), null);
        } catch (NumberFormatException e) {
            return new ValidationResult(false, null, "Invalid numeric mark: '" + clean + "'.");
        }
    }

    /**
     * Safe rounding helper to 2 decimal places using BigDecimal.
     */
    public static double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Encapsulates raw mark parsing and validation result.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final Double parsedValue;
        private final String errorMessage;

        public ValidationResult(boolean valid, Double parsedValue, String errorMessage) {
            this.valid = valid;
            this.parsedValue = parsedValue;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() { return valid; }
        public Double getParsedValue() { return parsedValue; }
        public String getErrorMessage() { return errorMessage; }
    }
}
