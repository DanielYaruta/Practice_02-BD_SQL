package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StudentImporter")
class StudentImporterTest {

    private static final String CSV_HEADER =
        "\"gender\",\"race/ethnicity\",\"parental level of education\","
        + "\"lunch\",\"test preparation course\","
        + "\"math score\",\"reading score\",\"writing score\"";

    private Connection connection;
    private Path tempCsv;

    @BeforeEach
    void setUp() throws SQLException, IOException {
        connection = DriverManager.getConnection(
            "jdbc:h2:mem:importtest_" + System.nanoTime(), "sa", "");
        DatabaseManager.createTable(connection);
        tempCsv = Files.createTempFile("students_test_", ".csv");
    }

    @AfterEach
    void tearDown() throws SQLException, IOException {
        connection.close();
        Files.deleteIfExists(tempCsv);
    }

    @Test
    @DisplayName("imports every data row and skips the header")
    void importFromCsv_returnsCorrectRowCount() throws IOException, SQLException {
        Files.writeString(tempCsv,
            CSV_HEADER + "\n"
            + "\"female\",\"group A\",\"bachelor's degree\",\"standard\",\"completed\",\"90\",\"85\",\"88\"\n"
            + "\"male\",\"group B\",\"master's degree\",\"free/reduced\",\"none\",\"70\",\"75\",\"72\"\n"
            + "\"female\",\"group C\",\"some college\",\"standard\",\"none\",\"60\",\"65\",\"62\"\n"
        );

        int imported = StudentImporter.importFromCsv(connection, tempCsv);

        assertEquals(3, imported);
    }

    @Test
    @DisplayName("imported rows are actually stored in the database")
    void importFromCsv_rowsPersistedInDb() throws IOException, SQLException {
        Files.writeString(tempCsv,
            CSV_HEADER + "\n"
            + "\"female\",\"group A\",\"bachelor's degree\",\"standard\",\"completed\",\"90\",\"85\",\"88\"\n"
            + "\"male\",\"group B\",\"master's degree\",\"free/reduced\",\"none\",\"70\",\"75\",\"72\"\n"
        );

        StudentImporter.importFromCsv(connection, tempCsv);

        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM students");
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertEquals(2, rs.getInt(1));
        }
    }

    @Test
    @DisplayName("field values are stored correctly (spot-check first row)")
    void importFromCsv_fieldValuesAreCorrect() throws IOException, SQLException {
        Files.writeString(tempCsv,
            CSV_HEADER + "\n"
            + "\"female\",\"group A\",\"bachelor's degree\",\"standard\",\"completed\",\"90\",\"85\",\"88\"\n"
        );

        StudentImporter.importFromCsv(connection, tempCsv);

        try (PreparedStatement ps = connection.prepareStatement(
                 "SELECT * FROM students ORDER BY id LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertAll(
                () -> assertEquals("female",            rs.getString("gender")),
                () -> assertEquals("group A",           rs.getString("race_ethnicity")),
                () -> assertEquals("bachelor's degree", rs.getString("parental_level_of_education")),
                () -> assertEquals("standard",          rs.getString("lunch")),
                () -> assertEquals("completed",         rs.getString("test_preparation_course")),
                () -> assertEquals(90,                  rs.getInt("math_score")),
                () -> assertEquals(85,                  rs.getInt("reading_score")),
                () -> assertEquals(88,                  rs.getInt("writing_score"))
            );
        }
    }

    @Test
    @DisplayName("returns 0 for a CSV with only a header line")
    void importFromCsv_headerOnlyFile() throws IOException, SQLException {
        Files.writeString(tempCsv, CSV_HEADER + "\n");

        int imported = StudentImporter.importFromCsv(connection, tempCsv);

        assertEquals(0, imported);
    }

    @Test
    @DisplayName("skips malformed rows without aborting the whole import")
    void importFromCsv_skipsMalformedRows() throws IOException, SQLException {
        Files.writeString(tempCsv,
            CSV_HEADER + "\n"
            + "\"female\",\"group A\",\"bachelor's degree\",\"standard\",\"completed\",\"90\",\"85\",\"88\"\n"
            + "\"bad\",\"row\"\n"                              // only 2 fields — skipped
            + "\"male\",\"group B\",\"high school\",\"standard\",\"none\",\"not-a-number\",\"70\",\"68\"\n" // bad score
            + "\"female\",\"group C\",\"some college\",\"free/reduced\",\"none\",\"60\",\"65\",\"62\"\n"
        );

        int imported = StudentImporter.importFromCsv(connection, tempCsv);

        assertEquals(2, imported);
    }

    @Test
    @DisplayName("printVerification runs without throwing after an import")
    void printVerification_doesNotThrow() throws IOException, SQLException {
        Files.writeString(tempCsv,
            CSV_HEADER + "\n"
            + "\"female\",\"group A\",\"bachelor's degree\",\"standard\",\"completed\",\"90\",\"85\",\"88\"\n"
        );
        StudentImporter.importFromCsv(connection, tempCsv);

        assertDoesNotThrow(() -> StudentImporter.printVerification(connection));
    }
}
