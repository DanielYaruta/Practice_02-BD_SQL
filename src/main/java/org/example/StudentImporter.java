package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentImporter {

    private static final String INSERT_SQL = """
            INSERT INTO students
                (gender, race_ethnicity, parental_level_of_education,
                 lunch, test_preparation_course, math_score, reading_score, writing_score)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final int BATCH_SIZE = 100;

    /**
     * Reads the CSV at csvPath and imports every data row into the students table.
     * The first line (header) is skipped automatically.
     *
     * @return number of rows successfully imported
     */
    public static int importFromCsv(Connection connection, Path csvPath) throws IOException, SQLException {

        int rowsImported = 0;
        int lineNumber = 0;

        connection.setAutoCommit(false);

        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8);
             PreparedStatement pstmt = connection.prepareStatement(INSERT_SQL)) {

            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (lineNumber == 1) {
                    System.out.println("[CSV] Header skipped: " + line);
                    continue;
                }

                line = line.trim();
                if (line.isEmpty()) continue;

                String[] fields = CsvParser.parseLine(line);

                if (fields.length != 8) {
                    System.err.printf("[WARN] Line %d has %d fields (expected 8), skipping: %s%n",
                            lineNumber, fields.length, line);
                    continue;
                }

                try {
                    pstmt.setString(1, fields[0]);
                    pstmt.setString(2, fields[1]);
                    pstmt.setString(3, fields[2]);
                    pstmt.setString(4, fields[3]);
                    pstmt.setString(5, fields[4]);
                    pstmt.setInt(6, Integer.parseInt(fields[5].trim()));
                    pstmt.setInt(7, Integer.parseInt(fields[6].trim()));
                    pstmt.setInt(8, Integer.parseInt(fields[7].trim()));

                    pstmt.addBatch();
                    rowsImported++;

                    if (rowsImported % BATCH_SIZE == 0) {
                        pstmt.executeBatch();
                        connection.commit();
                        System.out.printf("[IMPORT] %d rows inserted...%n", rowsImported);
                    }

                } catch (NumberFormatException e) {
                    System.err.printf("[WARN] Line %d has invalid score value, skipping: %s%n", lineNumber, line);
                }
            }

            pstmt.executeBatch();
            connection.commit();
        } catch (SQLException | IOException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }

        return rowsImported;
    }

    /**
     * Runs a verification query and prints summary statistics.
     */
    public static void printVerification(Connection connection) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM students";
        String statsSql = """
                SELECT
                    MIN(math_score)  AS min_math,
                    MAX(math_score)  AS max_math,
                    AVG(math_score)  AS avg_math,
                    MIN(reading_score) AS min_read,
                    MAX(reading_score) AS max_read,
                    AVG(reading_score) AS avg_read,
                    MIN(writing_score) AS min_write,
                    MAX(writing_score) AS max_write,
                    AVG(writing_score) AS avg_write
                FROM students
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(countSql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                System.out.println("\n========================================");
                System.out.println("  SELECT COUNT(*) FROM students;");
                System.out.println("  Result: " + rs.getInt(1) + " rows");
                System.out.println("========================================");
            }
        }

        System.out.println("\n--- Score Statistics ---");
        try (PreparedStatement pstmt = connection.prepareStatement(statsSql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                System.out.printf("Math    score: min=%d  max=%d  avg=%.2f%n",
                        rs.getInt("min_math"), rs.getInt("max_math"), rs.getDouble("avg_math"));
                System.out.printf("Reading score: min=%d  max=%d  avg=%.2f%n",
                        rs.getInt("min_read"), rs.getInt("max_read"), rs.getDouble("avg_read"));
                System.out.printf("Writing score: min=%d  max=%d  avg=%.2f%n",
                        rs.getInt("min_write"), rs.getInt("max_write"), rs.getDouble("avg_write"));
            }
        }

        System.out.println("\n--- First 5 rows ---");
        try (PreparedStatement pstmt = connection.prepareStatement(
                     "SELECT * FROM students ORDER BY id LIMIT 5");
             ResultSet rs = pstmt.executeQuery()) {
            System.out.printf("%-5s %-8s %-10s %-30s %-14s %-24s %-6s %-8s %-8s%n",
                    "ID", "Gender", "Race/Eth", "Parent Education",
                    "Lunch", "Test Prep", "Math", "Reading", "Writing");
            System.out.println("-".repeat(110));
            while (rs.next()) {
                System.out.printf("%-5d %-8s %-10s %-30s %-14s %-24s %-6d %-8d %-8d%n",
                        rs.getInt("id"),
                        rs.getString("gender"),
                        rs.getString("race_ethnicity"),
                        rs.getString("parental_level_of_education"),
                        rs.getString("lunch"),
                        rs.getString("test_preparation_course"),
                        rs.getInt("math_score"),
                        rs.getInt("reading_score"),
                        rs.getInt("writing_score"));
            }
        }
    }
}
