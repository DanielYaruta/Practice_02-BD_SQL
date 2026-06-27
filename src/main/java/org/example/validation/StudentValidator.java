package org.example.validation;

import org.example.model.Student;

import java.util.Set;

/**
 * Pure validation logic for Student data and DAO query parameters.
 * Every method returns a {@link ValidationResult} — callers decide
 * whether to throw, log, or aggregate errors.
 */
public final class StudentValidator {

    // ── allowed domain values ───────────────────────────────────────────────

    public static final Set<String> VALID_GENDERS = Set.of("male", "female");

    public static final Set<String> VALID_RACE_GROUPS =
        Set.of("group a", "group b", "group c", "group d", "group e");

    public static final Set<String> VALID_LUNCHES =
        Set.of("standard", "free/reduced");

    public static final Set<String> VALID_TEST_PREPS =
        Set.of("completed", "none");

    public static final Set<String> VALID_PARENTAL_EDUCATIONS = Set.of(
        "bachelor's degree", "some college", "master's degree",
        "associate's degree", "high school", "some high school"
    );

    private StudentValidator() {}

    // ── individual field validators ─────────────────────────────────────────

    public static ValidationResult validateGender(String gender) {
        if (gender == null || gender.isBlank())
            return ValidationResult.of("gender must not be blank");
        if (!VALID_GENDERS.contains(gender.toLowerCase()))
            return ValidationResult.of(
                "gender must be one of " + VALID_GENDERS + ", got: '" + gender + "'");
        return ValidationResult.ok();
    }

    public static ValidationResult validateRaceEthnicity(String raceEthnicity) {
        if (raceEthnicity == null || raceEthnicity.isBlank())
            return ValidationResult.of("race_ethnicity must not be blank");
        if (!VALID_RACE_GROUPS.contains(raceEthnicity.toLowerCase()))
            return ValidationResult.of(
                "race_ethnicity must be one of " + VALID_RACE_GROUPS + ", got: '" + raceEthnicity + "'");
        return ValidationResult.ok();
    }

    public static ValidationResult validateParentalEducation(String education) {
        if (education == null || education.isBlank())
            return ValidationResult.of("parental_level_of_education must not be blank");
        if (!VALID_PARENTAL_EDUCATIONS.contains(education.toLowerCase()))
            return ValidationResult.of(
                "parental_level_of_education must be one of " + VALID_PARENTAL_EDUCATIONS
                + ", got: '" + education + "'");
        return ValidationResult.ok();
    }

    public static ValidationResult validateLunch(String lunch) {
        if (lunch == null || lunch.isBlank())
            return ValidationResult.of("lunch must not be blank");
        if (!VALID_LUNCHES.contains(lunch.toLowerCase()))
            return ValidationResult.of(
                "lunch must be one of " + VALID_LUNCHES + ", got: '" + lunch + "'");
        return ValidationResult.ok();
    }

    public static ValidationResult validateTestPreparationCourse(String course) {
        if (course == null || course.isBlank())
            return ValidationResult.of("test_preparation_course must not be blank");
        if (!VALID_TEST_PREPS.contains(course.toLowerCase()))
            return ValidationResult.of(
                "test_preparation_course must be one of " + VALID_TEST_PREPS + ", got: '" + course + "'");
        return ValidationResult.ok();
    }

    /** Score must be in [0, 100]. */
    public static ValidationResult validateScore(String fieldName, int score) {
        if (score < 0 || score > 100)
            return ValidationResult.of(
                fieldName + " must be between 0 and 100, got: " + score);
        return ValidationResult.ok();
    }

    // ── query parameter validators ──────────────────────────────────────────

    /** Score threshold used in "above" queries must be in [0, 100]. */
    public static ValidationResult validateScoreThreshold(int threshold) {
        if (threshold < 0 || threshold > 100)
            return ValidationResult.of(
                "score threshold must be between 0 and 100, got: " + threshold);
        return ValidationResult.ok();
    }

    /** Row limit for top-N queries must be at least 1. */
    public static ValidationResult validateLimit(int limit) {
        if (limit < 1)
            return ValidationResult.of("limit must be at least 1, got: " + limit);
        return ValidationResult.ok();
    }

    /** Non-nullable string filter parameter (gender, race group, education…). */
    public static ValidationResult validateNonBlankFilter(String paramName, String value) {
        if (value == null || value.isBlank())
            return ValidationResult.of(paramName + " filter must not be blank");
        return ValidationResult.ok();
    }

    // ── composite validators ────────────────────────────────────────────────

    /**
     * Validates a parsed CSV row (8 String fields).
     * Collects ALL errors — does not stop at the first failure.
     */
    public static ValidationResult validateCsvRow(String[] fields) {
        if (fields == null)
            return ValidationResult.of("fields array must not be null");
        if (fields.length != 8)
            return ValidationResult.of(
                "expected 8 fields but got " + fields.length);

        ValidationResult result = validateGender(fields[0])
            .and(validateRaceEthnicity(fields[1]))
            .and(validateParentalEducation(fields[2]))
            .and(validateLunch(fields[3]))
            .and(validateTestPreparationCourse(fields[4]));

        String[] scoreNames = {"math_score", "reading_score", "writing_score"};
        for (int i = 0; i < 3; i++) {
            String raw = fields[5 + i] == null ? "" : fields[5 + i].trim();
            try {
                result = result.and(validateScore(scoreNames[i], Integer.parseInt(raw)));
            } catch (NumberFormatException e) {
                result = result.and(ValidationResult.of(
                    scoreNames[i] + " is not a valid integer: '" + raw + "'"));
            }
        }

        return result;
    }

    /**
     * Validates a fully constructed {@link Student} object.
     * Useful for validating data read back from the database or built programmatically.
     */
    public static ValidationResult validateStudent(Student student) {
        if (student == null)
            return ValidationResult.of("student must not be null");

        return validateGender(student.getGender())
            .and(validateRaceEthnicity(student.getRaceEthnicity()))
            .and(validateParentalEducation(student.getParentalLevelOfEducation()))
            .and(validateLunch(student.getLunch()))
            .and(validateTestPreparationCourse(student.getTestPreparationCourse()))
            .and(validateScore("math_score",    student.getMathScore()))
            .and(validateScore("reading_score", student.getReadingScore()))
            .and(validateScore("writing_score", student.getWritingScore()));
    }
}
