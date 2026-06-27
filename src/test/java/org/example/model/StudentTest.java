package org.example.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Student model")
class StudentTest {

    private Student student(int math, int reading, int writing) {
        return new Student(1, "female", "group A", "bachelor's degree",
                           "standard", "completed", math, reading, writing);
    }

    @Test
    @DisplayName("getAverageScore returns arithmetic mean of three scores")
    void getAverageScore_correctMean() {
        Student s = student(80, 90, 70);
        assertEquals(80.0, s.getAverageScore(), 0.001);
    }

    @Test
    @DisplayName("getAverageScore handles perfect score")
    void getAverageScore_perfectScore() {
        Student s = student(100, 100, 100);
        assertEquals(100.0, s.getAverageScore(), 0.001);
    }

    @Test
    @DisplayName("getAverageScore handles non-integer mean")
    void getAverageScore_fractionalMean() {
        // (90 + 85 + 88) / 3 = 87.666...
        Student s = student(90, 85, 88);
        assertEquals(87.666, s.getAverageScore(), 0.001);
    }

    @Test
    @DisplayName("getAverageScore handles zero scores")
    void getAverageScore_zeroScores() {
        Student s = student(0, 0, 0);
        assertEquals(0.0, s.getAverageScore(), 0.001);
    }

    @Test
    @DisplayName("getters return the values passed to the constructor")
    void getters_returnConstructorValues() {
        Student s = new Student(42, "male", "group E", "master's degree",
                                "free/reduced", "none", 78, 82, 75);

        assertAll(
            () -> assertEquals(42,               s.getId()),
            () -> assertEquals("male",           s.getGender()),
            () -> assertEquals("group E",        s.getRaceEthnicity()),
            () -> assertEquals("master's degree",s.getParentalLevelOfEducation()),
            () -> assertEquals("free/reduced",   s.getLunch()),
            () -> assertEquals("none",           s.getTestPreparationCourse()),
            () -> assertEquals(78,               s.getMathScore()),
            () -> assertEquals(82,               s.getReadingScore()),
            () -> assertEquals(75,               s.getWritingScore())
        );
    }

    @Test
    @DisplayName("toString contains id and all three scores")
    void toString_containsKeyFields() {
        Student s = new Student(7, "female", "group B", "some college",
                                "standard", "completed", 91, 88, 85);
        String str = s.toString();

        assertTrue(str.contains("7"));
        assertTrue(str.contains("91"));
        assertTrue(str.contains("88"));
        assertTrue(str.contains("85"));
    }
}
