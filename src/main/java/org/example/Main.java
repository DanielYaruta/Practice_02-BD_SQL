package org.example;

/*
 * ============================================================
 *  Task 1 + Task 2 — Students CSV Importer & DAO Demo
 * ============================================================
 *
 * HOW TO RUN (terminal):
 *   mvn package -q
 *   java -jar target/Practice_02-BD_SQL-1.0-SNAPSHOT-jar-with-dependencies.jar
 *
 * HOW TO RUN (IntelliJ IDEA):
 *   1. Open the project; Maven downloads H2 automatically.
 *   2. Place StudentsPerformance.csv in the project root (already there).
 *   3. Run Main.main() from the IDE.
 *
 * DATABASE:
 *   H2 file-based DB → students_db.mv.db (created in working directory)
 *   JDBC URL : jdbc:h2:./students_db
 *   User     : sa  |  Password: (empty)
 * ============================================================
 */

import org.example.dao.StudentDao;
import org.example.dao.StudentDaoImpl;
import org.example.model.Student;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        Path csvPath = Paths.get("StudentsPerformance.csv");

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   Students Performance — CSV Import & DAO Demo  ║");
        System.out.println("╚══════════════════════════════════════════════════╝");

        try (Connection connection = DatabaseManager.getConnection()) {

            // ── Task 1: ensure table exists and data is loaded ──────────
            DatabaseManager.createTable(connection);
            ensureDataImported(connection, csvPath);

            // ── Task 2: DAO demonstration ───────────────────────────────
            StudentDao dao = new StudentDaoImpl(connection);
            runDaoDemo(dao);

        } catch (SQLException e) {
            System.err.println("[ERROR] Database: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("[ERROR] CSV file not found: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ //
    //  Import guard — skips import if table already has rows              //
    // ------------------------------------------------------------------ //

    private static void ensureDataImported(Connection connection, Path csvPath)
            throws SQLException, IOException {

        int count = 0;
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT COUNT(*) FROM students");
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) count = rs.getInt(1);
        }

        if (count > 0) {
            System.out.printf("%n[DB] Table already contains %d rows — skipping import.%n", count);
        } else {
            System.out.println("\n[IMPORT] Table is empty, importing CSV...");
            int imported = StudentImporter.importFromCsv(connection, csvPath);
            System.out.printf("[IMPORT] Done: %d rows imported.%n", imported);
        }
    }

    // ------------------------------------------------------------------ //
    //  DAO demonstration                                                  //
    // ------------------------------------------------------------------ //

    private static void runDaoDemo(StudentDao dao) throws SQLException {

        // 1. findAll() ─────────────────────────────────────────────────
        section("1. findAll()  →  total rows in table");
        List<Student> all = dao.findAll();
        System.out.printf("Total students: %d%n", all.size());

        // 2. findByTestPreparationCourse("completed") ──────────────────
        section("2. findByTestPreparationCourse(\"completed\")");
        List<Student> completed = dao.findByTestPreparationCourse("completed");
        System.out.printf("Students who completed the test prep course: %d%n", completed.size());
        printTable(completed, 5);

        // 3. findByTestPreparationCourse("none") ───────────────────────
        section("3. findByTestPreparationCourse(\"none\")");
        List<Student> noCourse = dao.findByTestPreparationCourse("none");
        System.out.printf("Students with no test prep: %d%n", noCourse.size());
        printTable(noCourse, 5);

        // 4. findByMathScoreAbove(90) ──────────────────────────────────
        section("4. findByMathScoreAbove(90)  →  math score > 90");
        List<Student> highMath = dao.findByMathScoreAbove(90);
        System.out.printf("Students with math score > 90: %d%n", highMath.size());
        printTable(highMath, 10);

        // 5. findByReadingScoreAbove(95) ───────────────────────────────
        section("5. findByReadingScoreAbove(95)  →  reading score > 95");
        List<Student> highReading = dao.findByReadingScoreAbove(95);
        System.out.printf("Students with reading score > 95: %d%n", highReading.size());
        printTable(highReading, 10);

        // 6. findByGender("female") ────────────────────────────────────
        section("6. findByGender(\"female\")");
        List<Student> females = dao.findByGender("female");
        System.out.printf("Female students: %d%n", females.size());
        printTable(females, 5);

        // 7. findByRaceEthnicity("group E") ────────────────────────────
        section("7. findByRaceEthnicity(\"group E\")");
        List<Student> groupE = dao.findByRaceEthnicity("group E");
        System.out.printf("Students in group E: %d%n", groupE.size());
        printTable(groupE, 5);

        // 8. findByGenderAndRaceEthnicity("male", "group A") ───────────
        section("8. findByGenderAndRaceEthnicity(\"male\", \"group A\")");
        List<Student> maleGroupA = dao.findByGenderAndRaceEthnicity("male", "group A");
        System.out.printf("Male students in group A: %d%n", maleGroupA.size());
        printTable(maleGroupA, 8);

        // 9. findByAllScoresAbove(80) ──────────────────────────────────
        section("9. findByAllScoresAbove(80)  →  all three scores > 80");
        List<Student> allAbove80 = dao.findByAllScoresAbove(80);
        System.out.printf("Students with all scores > 80: %d%n", allAbove80.size());
        printTable(allAbove80, 8);

        // 10. findTopByAverageScore(10) ────────────────────────────────
        section("10. findTopByAverageScore(10)  →  top-10 by average score");
        List<Student> top10 = dao.findTopByAverageScore(10);
        printTable(top10, 10);

        // 11. findByParentalEducation("master's degree") ───────────────
        section("11. findByParentalEducation(\"master's degree\")");
        List<Student> mastersDegree = dao.findByParentalEducation("master's degree");
        System.out.printf("Students whose parent has a master's degree: %d%n", mastersDegree.size());
        printTable(mastersDegree, 5);

        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║   All DAO methods demonstrated   ║");
        System.out.println("╚══════════════════════════════════╝");
    }

    // ------------------------------------------------------------------ //
    //  Console output helpers                                             //
    // ------------------------------------------------------------------ //

    private static void section(String title) {
        System.out.println("\n┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────");
        System.out.println("│  " + title);
        System.out.println("└─────────────────────────────────────────────────────────────────────────────────────────────────────────────");
    }

    private static final String HDR_FMT  = "%-5s %-8s %-9s %-30s %-13s %-11s %-5s %-5s %-5s %-5s%n";
    private static final String ROW_FMT  = "%-5d %-8s %-9s %-30s %-13s %-11s %-5d %-5d %-5d %-5.1f%n";
    private static final String DIVIDER  = "─".repeat(108);

    private static void printTable(List<Student> students, int maxRows) {
        if (students.isEmpty()) {
            System.out.println("  (no rows)");
            return;
        }
        System.out.printf(HDR_FMT,
            "ID", "Gender", "Race/Eth", "Parental Education",
            "Lunch", "Test Prep", "Math", "Read", "Write", "Avg");
        System.out.println(DIVIDER);

        int printed = 0;
        for (Student s : students) {
            System.out.printf(ROW_FMT,
                s.getId(), s.getGender(), s.getRaceEthnicity(),
                s.getParentalLevelOfEducation(), s.getLunch(),
                s.getTestPreparationCourse(),
                s.getMathScore(), s.getReadingScore(), s.getWritingScore(),
                s.getAverageScore());
            if (++printed >= maxRows) break;
        }
        if (students.size() > maxRows) {
            System.out.printf("  ... and %d more rows%n", students.size() - maxRows);
        }
    }
}
