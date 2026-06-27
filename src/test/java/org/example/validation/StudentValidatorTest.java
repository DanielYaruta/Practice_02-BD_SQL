package org.example.validation;

import org.example.model.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StudentValidator")
class StudentValidatorTest {

    // ── ValidationResult core behaviour ────────────────────────────────────

    @Nested
    @DisplayName("ValidationResult")
    class ValidationResultTest {

        @Test
        @DisplayName("ok() is valid with no errors")
        void ok_isValid() {
            ValidationResult r = ValidationResult.ok();
            assertTrue(r.isValid());
            assertTrue(r.getErrors().isEmpty());
        }

        @Test
        @DisplayName("of(msg) is invalid with one error")
        void of_isInvalidWithOneError() {
            ValidationResult r = ValidationResult.of("something went wrong");
            assertFalse(r.isValid());
            assertEquals(1, r.getErrors().size());
            assertEquals("something went wrong", r.getErrors().get(0));
        }

        @Test
        @DisplayName("and() of two ok results is ok")
        void and_bothValid() {
            assertTrue(ValidationResult.ok().and(ValidationResult.ok()).isValid());
        }

        @Test
        @DisplayName("and() propagates errors from the left side")
        void and_leftInvalid() {
            ValidationResult r = ValidationResult.of("left error").and(ValidationResult.ok());
            assertFalse(r.isValid());
            assertEquals(1, r.getErrors().size());
        }

        @Test
        @DisplayName("and() propagates errors from the right side")
        void and_rightInvalid() {
            ValidationResult r = ValidationResult.ok().and(ValidationResult.of("right error"));
            assertFalse(r.isValid());
        }

        @Test
        @DisplayName("and() collects errors from both sides")
        void and_bothInvalid_accumulatesAllErrors() {
            ValidationResult r = ValidationResult.of("error A").and(ValidationResult.of("error B"));
            assertFalse(r.isValid());
            assertEquals(2, r.getErrors().size());
            assertTrue(r.getErrorMessage().contains("error A"));
            assertTrue(r.getErrorMessage().contains("error B"));
        }

        @Test
        @DisplayName("throwIfInvalid does not throw for a valid result")
        void throwIfInvalid_doesNotThrowWhenValid() {
            assertDoesNotThrow(() -> ValidationResult.ok().throwIfInvalid());
        }

        @Test
        @DisplayName("throwIfInvalid throws ValidationException for an invalid result")
        void throwIfInvalid_throwsWhenInvalid() {
            ValidationResult r = ValidationResult.of("bad value");
            ValidationException ex = assertThrows(ValidationException.class, r::throwIfInvalid);
            assertEquals("bad value", ex.getMessage());
            assertSame(r, ex.getValidationResult());
        }
    }

    // ── validateGender ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("validateGender")
    class ValidateGenderTest {

        @ParameterizedTest(name = "\"{0}\" is valid")
        @ValueSource(strings = {"male", "female", "Male", "FEMALE"})
        void valid(String gender) {
            assertTrue(StudentValidator.validateGender(gender).isValid());
        }

        @Test
        @DisplayName("null is invalid")
        void nullIsInvalid() {
            assertFalse(StudentValidator.validateGender(null).isValid());
        }

        @Test
        @DisplayName("blank string is invalid")
        void blankIsInvalid() {
            assertFalse(StudentValidator.validateGender("  ").isValid());
        }

        @Test
        @DisplayName("unknown value is invalid")
        void unknownValueIsInvalid() {
            ValidationResult r = StudentValidator.validateGender("other");
            assertFalse(r.isValid());
            assertTrue(r.getErrorMessage().contains("other"));
        }
    }

    // ── validateRaceEthnicity ───────────────────────────────────────────────

    @Nested
    @DisplayName("validateRaceEthnicity")
    class ValidateRaceEthnicityTest {

        @ParameterizedTest(name = "\"{0}\" is valid")
        @ValueSource(strings = {"group a", "group b", "group c", "group d", "group e",
                                "Group A", "GROUP E"})
        void valid(String race) {
            assertTrue(StudentValidator.validateRaceEthnicity(race).isValid());
        }

        @Test
        @DisplayName("null is invalid")
        void nullIsInvalid() {
            assertFalse(StudentValidator.validateRaceEthnicity(null).isValid());
        }

        @Test
        @DisplayName("unknown group is invalid")
        void unknownGroupIsInvalid() {
            assertFalse(StudentValidator.validateRaceEthnicity("group z").isValid());
        }
    }

    // ── validateParentalEducation ───────────────────────────────────────────

    @Nested
    @DisplayName("validateParentalEducation")
    class ValidateParentalEducationTest {

        @ParameterizedTest(name = "\"{0}\" is valid")
        @ValueSource(strings = {
            "bachelor's degree", "some college", "master's degree",
            "associate's degree", "high school", "some high school"
        })
        void valid(String edu) {
            assertTrue(StudentValidator.validateParentalEducation(edu).isValid());
        }

        @Test
        @DisplayName("null is invalid")
        void nullIsInvalid() {
            assertFalse(StudentValidator.validateParentalEducation(null).isValid());
        }

        @Test
        @DisplayName("arbitrary string is invalid")
        void unknownIsInvalid() {
            assertFalse(StudentValidator.validateParentalEducation("phd").isValid());
        }
    }

    // ── validateLunch ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("validateLunch")
    class ValidateLunchTest {

        @ParameterizedTest(name = "\"{0}\" is valid")
        @ValueSource(strings = {"standard", "free/reduced", "Standard", "FREE/REDUCED"})
        void valid(String lunch) {
            assertTrue(StudentValidator.validateLunch(lunch).isValid());
        }

        @Test
        @DisplayName("null is invalid")
        void nullIsInvalid() {
            assertFalse(StudentValidator.validateLunch(null).isValid());
        }

        @Test
        @DisplayName("unknown value is invalid")
        void unknownIsInvalid() {
            assertFalse(StudentValidator.validateLunch("premium").isValid());
        }
    }

    // ── validateTestPreparationCourse ───────────────────────────────────────

    @Nested
    @DisplayName("validateTestPreparationCourse")
    class ValidateTestPrepTest {

        @ParameterizedTest(name = "\"{0}\" is valid")
        @ValueSource(strings = {"completed", "none", "Completed", "NONE"})
        void valid(String course) {
            assertTrue(StudentValidator.validateTestPreparationCourse(course).isValid());
        }

        @Test
        @DisplayName("null is invalid")
        void nullIsInvalid() {
            assertFalse(StudentValidator.validateTestPreparationCourse(null).isValid());
        }

        @Test
        @DisplayName("arbitrary value is invalid")
        void unknownIsInvalid() {
            assertFalse(StudentValidator.validateTestPreparationCourse("in progress").isValid());
        }
    }

    // ── validateScore ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("validateScore")
    class ValidateScoreTest {

        @ParameterizedTest(name = "{0} is valid")
        @ValueSource(ints = {0, 1, 50, 99, 100})
        void valid(int score) {
            assertTrue(StudentValidator.validateScore("math_score", score).isValid());
        }

        @ParameterizedTest(name = "{0} is invalid (out of [0,100])")
        @ValueSource(ints = {-1, -100, 101, 200})
        void invalid(int score) {
            ValidationResult r = StudentValidator.validateScore("math_score", score);
            assertFalse(r.isValid());
            assertTrue(r.getErrorMessage().contains(String.valueOf(score)));
        }
    }

    // ── validateScoreThreshold ──────────────────────────────────────────────

    @Nested
    @DisplayName("validateScoreThreshold")
    class ValidateScoreThresholdTest {

        @ParameterizedTest(name = "{0} is valid threshold")
        @ValueSource(ints = {0, 50, 100})
        void valid(int threshold) {
            assertTrue(StudentValidator.validateScoreThreshold(threshold).isValid());
        }

        @ParameterizedTest(name = "{0} is invalid threshold")
        @ValueSource(ints = {-1, 101})
        void invalid(int threshold) {
            assertFalse(StudentValidator.validateScoreThreshold(threshold).isValid());
        }
    }

    // ── validateLimit ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("validateLimit")
    class ValidateLimitTest {

        @ParameterizedTest(name = "{0} is valid limit")
        @ValueSource(ints = {1, 10, 1000})
        void valid(int limit) {
            assertTrue(StudentValidator.validateLimit(limit).isValid());
        }

        @ParameterizedTest(name = "{0} is invalid limit")
        @ValueSource(ints = {0, -1, -100})
        void invalid(int limit) {
            assertFalse(StudentValidator.validateLimit(limit).isValid());
        }
    }

    // ── validateNonBlankFilter ──────────────────────────────────────────────

    @Nested
    @DisplayName("validateNonBlankFilter")
    class ValidateNonBlankFilterTest {

        @Test
        @DisplayName("non-blank value is valid")
        void nonBlankIsValid() {
            assertTrue(StudentValidator.validateNonBlankFilter("gender", "female").isValid());
        }

        @Test
        @DisplayName("null is invalid")
        void nullIsInvalid() {
            assertFalse(StudentValidator.validateNonBlankFilter("gender", null).isValid());
        }

        @Test
        @DisplayName("blank string is invalid")
        void blankIsInvalid() {
            assertFalse(StudentValidator.validateNonBlankFilter("gender", "  ").isValid());
        }

        @Test
        @DisplayName("error message contains the param name")
        void errorContainsParamName() {
            ValidationResult r = StudentValidator.validateNonBlankFilter("gender", null);
            assertTrue(r.getErrorMessage().contains("gender"));
        }
    }

    // ── validateCsvRow ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("validateCsvRow")
    class ValidateCsvRowTest {

        private final String[] VALID_ROW = {
            "female", "group B", "bachelor's degree",
            "standard", "completed", "90", "85", "88"
        };

        @Test
        @DisplayName("valid row passes")
        void validRow() {
            assertTrue(StudentValidator.validateCsvRow(VALID_ROW).isValid());
        }

        @Test
        @DisplayName("null array is invalid")
        void nullArray() {
            assertFalse(StudentValidator.validateCsvRow(null).isValid());
        }

        @Test
        @DisplayName("wrong field count is invalid")
        void wrongFieldCount() {
            ValidationResult r = StudentValidator.validateCsvRow(new String[]{"a", "b"});
            assertFalse(r.isValid());
            assertTrue(r.getErrorMessage().contains("2"));
        }

        @Test
        @DisplayName("non-numeric score is invalid")
        void nonNumericScore() {
            String[] row = VALID_ROW.clone();
            row[5] = "not-a-number";
            ValidationResult r = StudentValidator.validateCsvRow(row);
            assertFalse(r.isValid());
            assertTrue(r.getErrorMessage().contains("math_score"));
        }

        @Test
        @DisplayName("score out of range is invalid")
        void scoreOutOfRange() {
            String[] row = VALID_ROW.clone();
            row[7] = "150";
            ValidationResult r = StudentValidator.validateCsvRow(row);
            assertFalse(r.isValid());
            assertTrue(r.getErrorMessage().contains("writing_score"));
        }

        @Test
        @DisplayName("invalid gender is invalid")
        void invalidGender() {
            String[] row = VALID_ROW.clone();
            row[0] = "unknown";
            assertFalse(StudentValidator.validateCsvRow(row).isValid());
        }

        @Test
        @DisplayName("multiple errors are all collected")
        void multipleErrorsAccumulated() {
            String[] row = {"bad_gender", "bad_race", "bad_edu",
                            "bad_lunch", "bad_prep", "NaN", "NaN", "NaN"};
            ValidationResult r = StudentValidator.validateCsvRow(row);
            assertFalse(r.isValid());
            assertTrue(r.getErrors().size() > 1,
                "expected multiple errors but got: " + r.getErrors());
        }
    }

    // ── validateStudent ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("validateStudent")
    class ValidateStudentTest {

        @Test
        @DisplayName("valid student passes")
        void validStudent() {
            Student s = new Student(1, "female", "group A", "bachelor's degree",
                                    "standard", "completed", 90, 85, 88);
            assertTrue(StudentValidator.validateStudent(s).isValid());
        }

        @Test
        @DisplayName("null student is invalid")
        void nullStudent() {
            assertFalse(StudentValidator.validateStudent(null).isValid());
        }

        @Test
        @DisplayName("student with out-of-range score is invalid")
        void invalidScore() {
            Student s = new Student(1, "male", "group B", "high school",
                                    "standard", "none", -5, 70, 65);
            ValidationResult r = StudentValidator.validateStudent(s);
            assertFalse(r.isValid());
            assertTrue(r.getErrorMessage().contains("math_score"));
        }

        @Test
        @DisplayName("student with invalid gender is invalid")
        void invalidGender() {
            Student s = new Student(1, "unknown", "group A", "high school",
                                    "standard", "none", 70, 70, 70);
            assertFalse(StudentValidator.validateStudent(s).isValid());
        }
    }
}
