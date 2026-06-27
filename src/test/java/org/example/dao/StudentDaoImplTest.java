package org.example.dao;

import org.example.DatabaseManager;
import org.example.model.Student;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Test data (5 students):
 *
 * id | gender | race   | parentEdu           | lunch        | prep      | math | read | write | avg
 * ---+--------+--------+---------------------+--------------+-----------+------+------+-------+-------
 *  1 | female | group A| bachelor's degree   | standard     | completed |  90  |  85  |  88   | 87.67
 *  2 | male   | group B| master's degree     | free/reduced | none      |  70  |  75  |  72   | 72.33
 *  3 | female | group A| some college        | standard     | none      |  60  |  65  |  62   | 62.33
 *  4 | male   | group A| high school         | standard     | completed |  95  |  92  |  90   | 92.33
 *  5 | female | group C| bachelor's degree   | free/reduced | none      |  50  |  55  |  52   | 52.33
 */
@DisplayName("StudentDaoImpl")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StudentDaoImplTest {

    private Connection connection;
    private StudentDao dao;

    @BeforeAll
    void setUpConnection() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:daoimpltest;DB_CLOSE_DELAY=-1", "sa", "");
        dao = new StudentDaoImpl(connection);
    }

    @BeforeEach
    void resetData() throws SQLException {
        // Drop and recreate so auto-increment resets — IDs are predictable
        try (PreparedStatement ps = connection.prepareStatement("DROP TABLE IF EXISTS students")) {
            ps.execute();
        }
        DatabaseManager.createTable(connection);
        insertTestData();
    }

    @AfterAll
    void tearDownConnection() throws SQLException {
        connection.close();
    }

    // ── findAll ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll returns all 5 students")
    void findAll_returnsAllRows() throws SQLException {
        assertEquals(5, dao.findAll().size());
    }

    // ── findByTestPreparationCourse ──────────────────────────────────────────

    @Test
    @DisplayName("findByTestPreparationCourse(completed) returns 2 students")
    void findByTestPrep_completed() throws SQLException {
        List<Student> result = dao.findByTestPreparationCourse("completed");
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(s -> "completed".equals(s.getTestPreparationCourse())));
    }

    @Test
    @DisplayName("findByTestPreparationCourse(none) returns 3 students")
    void findByTestPrep_none() throws SQLException {
        List<Student> result = dao.findByTestPreparationCourse("none");
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(s -> "none".equals(s.getTestPreparationCourse())));
    }

    // ── findByMathScoreAbove ──────────────────────────────────────────────────

    @Test
    @DisplayName("findByMathScoreAbove(80) returns students with math > 80")
    void findByMathScoreAbove_above80() throws SQLException {
        List<Student> result = dao.findByMathScoreAbove(80);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(s -> s.getMathScore() > 80));
    }

    @Test
    @DisplayName("findByMathScoreAbove(95) returns empty list when no one qualifies")
    void findByMathScoreAbove_noResults() throws SQLException {
        List<Student> result = dao.findByMathScoreAbove(95);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByMathScoreAbove result is ordered by math score descending")
    void findByMathScoreAbove_orderedDescending() throws SQLException {
        List<Student> result = dao.findByMathScoreAbove(60);
        for (int i = 1; i < result.size(); i++) {
            assertTrue(result.get(i - 1).getMathScore() >= result.get(i).getMathScore());
        }
    }

    // ── findByReadingScoreAbove ───────────────────────────────────────────────

    @Test
    @DisplayName("findByReadingScoreAbove(70) returns 3 students")
    void findByReadingScoreAbove_above70() throws SQLException {
        List<Student> result = dao.findByReadingScoreAbove(70);
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(s -> s.getReadingScore() > 70));
    }

    // ── findByGender ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("findByGender(female) returns 3 female students")
    void findByGender_female() throws SQLException {
        List<Student> result = dao.findByGender("female");
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(s -> "female".equals(s.getGender())));
    }

    @Test
    @DisplayName("findByGender(male) returns 2 male students")
    void findByGender_male() throws SQLException {
        List<Student> result = dao.findByGender("male");
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(s -> "male".equals(s.getGender())));
    }

    // ── findByRaceEthnicity ───────────────────────────────────────────────────

    @Test
    @DisplayName("findByRaceEthnicity(group A) returns 3 students")
    void findByRaceEthnicity_groupA() throws SQLException {
        List<Student> result = dao.findByRaceEthnicity("group A");
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(s -> "group A".equals(s.getRaceEthnicity())));
    }

    @Test
    @DisplayName("findByRaceEthnicity for a group not in data returns empty list")
    void findByRaceEthnicity_noResults() throws SQLException {
        assertTrue(dao.findByRaceEthnicity("group E").isEmpty());
    }

    // ── findByGenderAndRaceEthnicity ──────────────────────────────────────────

    @Test
    @DisplayName("findByGenderAndRaceEthnicity(male, group A) returns 1 student")
    void findByGenderAndRace_maleGroupA() throws SQLException {
        List<Student> result = dao.findByGenderAndRaceEthnicity("male", "group A");
        assertEquals(1, result.size());
        assertEquals("male",    result.get(0).getGender());
        assertEquals("group A", result.get(0).getRaceEthnicity());
        assertEquals(95,        result.get(0).getMathScore());
    }

    @Test
    @DisplayName("findByGenderAndRaceEthnicity(null, group A) delegates to findByRaceEthnicity")
    void findByGenderAndRace_nullGender() throws SQLException {
        List<Student> result = dao.findByGenderAndRaceEthnicity(null, "group A");
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("findByGenderAndRaceEthnicity(female, null) delegates to findByGender")
    void findByGenderAndRace_nullRace() throws SQLException {
        List<Student> result = dao.findByGenderAndRaceEthnicity("female", null);
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("findByGenderAndRaceEthnicity(null, null) delegates to findAll")
    void findByGenderAndRace_bothNull() throws SQLException {
        List<Student> result = dao.findByGenderAndRaceEthnicity(null, null);
        assertEquals(5, result.size());
    }

    // ── findByAllScoresAbove ──────────────────────────────────────────────────

    @Test
    @DisplayName("findByAllScoresAbove(80) returns only students where every score > 80")
    void findByAllScoresAbove_above80() throws SQLException {
        List<Student> result = dao.findByAllScoresAbove(80);
        assertEquals(2, result.size());
        for (Student s : result) {
            assertAll(
                () -> assertTrue(s.getMathScore()    > 80),
                () -> assertTrue(s.getReadingScore() > 80),
                () -> assertTrue(s.getWritingScore() > 80)
            );
        }
    }

    @Test
    @DisplayName("findByAllScoresAbove uses strict inequality — score equal to threshold is excluded")
    void findByAllScoresAbove_strictInequality() throws SQLException {
        // student 3 has math=60, read=65, write=62 — none are > 60 strictly
        List<Student> result = dao.findByAllScoresAbove(60);
        assertEquals(3, result.size());
        assertTrue(result.stream().noneMatch(s -> s.getMathScore() == 60));
    }

    // ── findTopByAverageScore ─────────────────────────────────────────────────

    @Test
    @DisplayName("findTopByAverageScore(1) returns exactly 1 student with highest total")
    void findTopByAverageScore_limit1() throws SQLException {
        List<Student> result = dao.findTopByAverageScore(1);
        assertEquals(1, result.size());
        assertEquals(95, result.get(0).getMathScore()); // student 4: total 277
    }

    @Test
    @DisplayName("findTopByAverageScore(3) returns 3 students ordered by total score desc")
    void findTopByAverageScore_limit3_orderedDesc() throws SQLException {
        List<Student> result = dao.findTopByAverageScore(3);
        assertEquals(3, result.size());
        // verify descending order by total score
        for (int i = 1; i < result.size(); i++) {
            int prevTotal = result.get(i - 1).getMathScore()
                          + result.get(i - 1).getReadingScore()
                          + result.get(i - 1).getWritingScore();
            int currTotal = result.get(i).getMathScore()
                          + result.get(i).getReadingScore()
                          + result.get(i).getWritingScore();
            assertTrue(prevTotal >= currTotal);
        }
    }

    @Test
    @DisplayName("findTopByAverageScore respects the limit even when more rows exist")
    void findTopByAverageScore_limitRespected() throws SQLException {
        List<Student> result = dao.findTopByAverageScore(2);
        assertEquals(2, result.size());
    }

    // ── findByParentalEducation ───────────────────────────────────────────────

    @Test
    @DisplayName("findByParentalEducation returns matching students")
    void findByParentalEducation_bachelorsDegree() throws SQLException {
        List<Student> result = dao.findByParentalEducation("bachelor's degree");
        assertEquals(2, result.size());
        assertTrue(result.stream()
            .allMatch(s -> "bachelor's degree".equals(s.getParentalLevelOfEducation())));
    }

    @Test
    @DisplayName("findByParentalEducation returns empty list for an absent education level")
    void findByParentalEducation_noResults() throws SQLException {
        assertTrue(dao.findByParentalEducation("associate's degree").isEmpty());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void insertTestData() throws SQLException {
        String sql = "INSERT INTO students "
            + "(gender,race_ethnicity,parental_level_of_education,lunch,"
            + "test_preparation_course,math_score,reading_score,writing_score) "
            + "VALUES (?,?,?,?,?,?,?,?)";

        Object[][] rows = {
            {"female", "group A", "bachelor's degree", "standard",     "completed", 90, 85, 88},
            {"male",   "group B", "master's degree",   "free/reduced", "none",      70, 75, 72},
            {"female", "group A", "some college",      "standard",     "none",      60, 65, 62},
            {"male",   "group A", "high school",       "standard",     "completed", 95, 92, 90},
            {"female", "group C", "bachelor's degree", "free/reduced", "none",      50, 55, 52},
        };

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Object[] row : rows) {
                ps.setString(1, (String) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setString(3, (String) row[2]);
                ps.setString(4, (String) row[3]);
                ps.setString(5, (String) row[4]);
                ps.setInt(6,    (int)    row[5]);
                ps.setInt(7,    (int)    row[6]);
                ps.setInt(8,    (int)    row[7]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
