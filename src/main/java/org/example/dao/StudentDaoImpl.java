package org.example.dao;

import org.example.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC-based implementation of StudentDao.
 *
 * The Connection is injected via the constructor so that the caller controls
 * its lifecycle (and can wrap multiple DAO calls in a single transaction).
 * Every method uses try-with-resources for PreparedStatement and ResultSet.
 */
public class StudentDaoImpl implements StudentDao {

    private static final String SELECT_ALL = "SELECT * FROM students";

    private final Connection connection;

    public StudentDaoImpl(Connection connection) {
        this.connection = connection;
    }

    // ------------------------------------------------------------------ //
    //  Helper                                                              //
    // ------------------------------------------------------------------ //

    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
            rs.getInt("id"),
            rs.getString("gender"),
            rs.getString("race_ethnicity"),
            rs.getString("parental_level_of_education"),
            rs.getString("lunch"),
            rs.getString("test_preparation_course"),
            rs.getInt("math_score"),
            rs.getInt("reading_score"),
            rs.getInt("writing_score")
        );
    }

    private List<Student> queryList(String sql, StatementPreparer preparer) throws SQLException {
        List<Student> result = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            if (preparer != null) {
                preparer.prepare(pstmt);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    @FunctionalInterface
    private interface StatementPreparer {
        void prepare(PreparedStatement pstmt) throws SQLException;
    }

    // ------------------------------------------------------------------ //
    //  Interface methods                                                   //
    // ------------------------------------------------------------------ //

    @Override
    public List<Student> findAll() throws SQLException {
        return queryList(SELECT_ALL, null);
    }

    @Override
    public List<Student> findByTestPreparationCourse(String course) throws SQLException {
        String sql = SELECT_ALL + " WHERE test_preparation_course = ?";
        return queryList(sql, pstmt -> pstmt.setString(1, course));
    }

    @Override
    public List<Student> findByMathScoreAbove(int minScore) throws SQLException {
        String sql = SELECT_ALL + " WHERE math_score > ? ORDER BY math_score DESC";
        return queryList(sql, pstmt -> pstmt.setInt(1, minScore));
    }

    @Override
    public List<Student> findByReadingScoreAbove(int minScore) throws SQLException {
        String sql = SELECT_ALL + " WHERE reading_score > ? ORDER BY reading_score DESC";
        return queryList(sql, pstmt -> pstmt.setInt(1, minScore));
    }

    @Override
    public List<Student> findByGender(String gender) throws SQLException {
        String sql = SELECT_ALL + " WHERE gender = ?";
        return queryList(sql, pstmt -> pstmt.setString(1, gender));
    }

    @Override
    public List<Student> findByRaceEthnicity(String raceEthnicity) throws SQLException {
        String sql = SELECT_ALL + " WHERE race_ethnicity = ?";
        return queryList(sql, pstmt -> pstmt.setString(1, raceEthnicity));
    }

    @Override
    public List<Student> findByGenderAndRaceEthnicity(String gender, String raceEthnicity) throws SQLException {
        if (gender == null && raceEthnicity == null) {
            return findAll();
        }
        if (gender == null) {
            return findByRaceEthnicity(raceEthnicity);
        }
        if (raceEthnicity == null) {
            return findByGender(gender);
        }
        String sql = SELECT_ALL + " WHERE gender = ? AND race_ethnicity = ?";
        return queryList(sql, pstmt -> {
            pstmt.setString(1, gender);
            pstmt.setString(2, raceEthnicity);
        });
    }

    @Override
    public List<Student> findByAllScoresAbove(int minScore) throws SQLException {
        String sql = SELECT_ALL + " WHERE math_score > ? AND reading_score > ? AND writing_score > ?"
                   + " ORDER BY (math_score + reading_score + writing_score) DESC";
        return queryList(sql, pstmt -> {
            pstmt.setInt(1, minScore);
            pstmt.setInt(2, minScore);
            pstmt.setInt(3, minScore);
        });
    }

    @Override
    public List<Student> findTopByAverageScore(int limit) throws SQLException {
        String sql = SELECT_ALL + " ORDER BY (math_score + reading_score + writing_score) DESC LIMIT ?";
        return queryList(sql, pstmt -> pstmt.setInt(1, limit));
    }

    @Override
    public List<Student> findByParentalEducation(String education) throws SQLException {
        String sql = SELECT_ALL + " WHERE parental_level_of_education = ?";
        return queryList(sql, pstmt -> pstmt.setString(1, education));
    }
}
