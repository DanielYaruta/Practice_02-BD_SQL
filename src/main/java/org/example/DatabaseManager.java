package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:h2:./students_db;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void createTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS students (
                    id                          INTEGER AUTO_INCREMENT PRIMARY KEY,
                    gender                      VARCHAR(10)  NOT NULL,
                    race_ethnicity              VARCHAR(20)  NOT NULL,
                    parental_level_of_education VARCHAR(50)  NOT NULL,
                    lunch                       VARCHAR(20)  NOT NULL,
                    test_preparation_course     VARCHAR(20)  NOT NULL,
                    math_score                  INTEGER      NOT NULL,
                    reading_score               INTEGER      NOT NULL,
                    writing_score               INTEGER      NOT NULL
                )
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("[DB] Table 'students' created (or already exists).");
        }
    }
}
