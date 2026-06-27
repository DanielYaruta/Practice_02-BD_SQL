package org.example.dao;

import org.example.model.Student;

import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object for the students table.
 * All methods throw SQLException so callers can decide on error handling.
 */
public interface StudentDao {

    /** Returns every row from the students table. */
    List<Student> findAll() throws SQLException;

    /**
     * Returns students whose test_preparation_course matches the given value.
     * @param course "completed" or "none"
     */
    List<Student> findByTestPreparationCourse(String course) throws SQLException;

    /**
     * Returns students whose math_score is strictly greater than minScore.
     * @param minScore lower bound (exclusive)
     */
    List<Student> findByMathScoreAbove(int minScore) throws SQLException;

    /**
     * Returns students whose reading_score is strictly greater than minScore.
     */
    List<Student> findByReadingScoreAbove(int minScore) throws SQLException;

    /**
     * Returns students filtered by gender.
     * @param gender "male" or "female"
     */
    List<Student> findByGender(String gender) throws SQLException;

    /**
     * Returns students filtered by race/ethnicity group.
     * @param raceEthnicity e.g. "group A", "group B", ...
     */
    List<Student> findByRaceEthnicity(String raceEthnicity) throws SQLException;

    /**
     * Returns students matching both gender and race/ethnicity.
     * Either parameter may be null to skip that filter.
     */
    List<Student> findByGenderAndRaceEthnicity(String gender, String raceEthnicity) throws SQLException;

    /**
     * Returns students where every score (math, reading, writing) is above minScore.
     * @param minScore lower bound (exclusive) applied to all three subjects
     */
    List<Student> findByAllScoresAbove(int minScore) throws SQLException;

    /**
     * Returns the top-N students ordered by their average score descending.
     * @param limit number of rows to return
     */
    List<Student> findTopByAverageScore(int limit) throws SQLException;

    /**
     * Returns students whose parental level of education matches the given value.
     * @param education e.g. "bachelor's degree", "master's degree", "some college", ...
     */
    List<Student> findByParentalEducation(String education) throws SQLException;
}
