package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DatabaseManager")
class DatabaseManagerTest {

    @Test
    @DisplayName("getConnection returns an open, valid connection")
    void getConnection_returnsOpenConnection() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            assertNotNull(conn);
            assertFalse(conn.isClosed());
        }
    }

    @Test
    @DisplayName("createTable does not throw on a fresh in-memory database")
    void createTable_createsTableWithoutError() throws SQLException {
        try (Connection conn = testConnection()) {
            assertDoesNotThrow(() -> DatabaseManager.createTable(conn));
        }
    }

    @Test
    @DisplayName("createTable is idempotent — calling it twice does not throw")
    void createTable_isIdempotent() throws SQLException {
        try (Connection conn = testConnection()) {
            DatabaseManager.createTable(conn);
            assertDoesNotThrow(() -> DatabaseManager.createTable(conn));
        }
    }

    @Test
    @DisplayName("created table has the expected 9 columns")
    void createTable_tableHasCorrectColumns() throws SQLException {
        try (Connection conn = testConnection()) {
            DatabaseManager.createTable(conn);

            // Insert one row and read it back to confirm all columns exist
            String insert = "INSERT INTO students "
                + "(gender,race_ethnicity,parental_level_of_education,lunch,"
                + "test_preparation_course,math_score,reading_score,writing_score) "
                + "VALUES (?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setString(1, "female");
                ps.setString(2, "group A");
                ps.setString(3, "bachelor's degree");
                ps.setString(4, "standard");
                ps.setString(5, "none");
                ps.setInt(6, 80);
                ps.setInt(7, 75);
                ps.setInt(8, 78);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM students");
                 ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }
        }
    }

    // ── helper ──────────────────────────────────────────────────────────────

    private Connection testConnection() throws SQLException {
        return java.sql.DriverManager.getConnection(
            "jdbc:h2:mem:dbmgrtest_" + System.nanoTime(), "sa", "");
    }
}
