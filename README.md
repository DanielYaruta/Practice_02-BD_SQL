# Practice 02 — BD SQL: CSV Import & DAO

Java + JDBC project that imports the [Students Performance in Exams](https://www.kaggle.com/datasets/spscientist/students-performance-in-exams) dataset from a CSV file into an H2 relational database and exposes a full DAO layer for querying the data.

## Technologies

| Tool | Version |
|---|---|
| Java | 17 |
| Maven | 3.9+ |
| H2 Database | 2.2.224 (embedded, file-based) |
| JDBC | standard (`java.sql`) |

## Project Structure

```
Practice_02-BD_SQL/
├── StudentsPerformance.csv          # Kaggle dataset (1 000 rows)
├── pom.xml
└── src/main/java/org/example/
    ├── Main.java                    # Entry point — runs import + DAO demo
    ├── DatabaseManager.java         # Connection factory + CREATE TABLE
    ├── StudentImporter.java         # Batch CSV → DB import (Task 1)
    ├── CsvParser.java               # RFC-4180 CSV line parser
    ├── model/
    │   └── Student.java             # Entity class
    └── dao/
        ├── StudentDao.java          # DAO interface (10 filter methods)
        └── StudentDaoImpl.java      # JDBC implementation
```

## Prerequisites

- JDK 17+ on `PATH`
- Maven 3.9+ on `PATH` (or use the IntelliJ built-in Maven)
- `StudentsPerformance.csv` in the **project root** (already included)

## How to Run

### Terminal

```bash
# 1. Build a fat JAR (includes H2 driver)
mvn package -q

# 2. Run from the project root (CSV must be in the working directory)
java -jar target/Practice_02-BD_SQL-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### IntelliJ IDEA

1. Open the project — Maven imports the H2 dependency automatically.
2. Run `org.example.Main`.

On first run the program creates `students_db.mv.db` in the working directory and imports all 1 000 rows. On subsequent runs the import step is skipped automatically.

## Database

| Parameter | Value |
|---|---|
| JDBC URL | `jdbc:h2:./students_db` |
| User | `sa` |
| Password | *(empty)* |

You can browse the database with the H2 Console (`mvn exec:java -Dexec.mainClass=org.h2.tools.Console`).

---

## Task 1 — CSV Import

`StudentImporter.importFromCsv()` reads `StudentsPerformance.csv` line by line and inserts every row using a `PreparedStatement` in batches of 100 inside a single transaction.

**Table schema:**

```sql
CREATE TABLE IF NOT EXISTS students (
    id                          INTEGER AUTO_INCREMENT PRIMARY KEY,
    gender                      VARCHAR(10),
    race_ethnicity              VARCHAR(20),
    parental_level_of_education VARCHAR(50),
    lunch                       VARCHAR(20),
    test_preparation_course     VARCHAR(20),
    math_score                  INTEGER,
    reading_score               INTEGER,
    writing_score               INTEGER
);
```

**Verification query printed at runtime:**

```
SELECT COUNT(*) FROM students;
Result: 1000 rows
```

---

## Task 2 — DAO Layer

`StudentDao` interface with 10 methods, implemented in `StudentDaoImpl` using `PreparedStatement` and `try-with-resources` throughout.

### Available filter methods

| Method | Description |
|---|---|
| `findAll()` | All 1 000 students |
| `findByTestPreparationCourse(course)` | `"completed"` → 358 rows / `"none"` → 642 rows |
| `findByMathScoreAbove(minScore)` | Math score strictly above threshold (ordered DESC) |
| `findByReadingScoreAbove(minScore)` | Reading score strictly above threshold (ordered DESC) |
| `findByGender(gender)` | `"female"` → 518 / `"male"` → 482 |
| `findByRaceEthnicity(group)` | By group A – E |
| `findByGenderAndRaceEthnicity(gender, group)` | Combined filter; either param may be `null` |
| `findByAllScoresAbove(minScore)` | All three scores above threshold |
| `findTopByAverageScore(limit)` | Top-N by average score |
| `findByParentalEducation(education)` | By parental level of education |

### Sample output (top-10 by average score)

```
ID    Gender   Race/Eth  Parental Education             Lunch         Test Prep   Math  Read  Write Avg
────────────────────────────────────────────────────────────────────────────────────────────────────────
963   female   group E   associate's degree             standard      none        100   100   100   100,0
917   male     group E   bachelor's degree              standard      completed   100   100   100   100,0
459   female   group E   bachelor's degree              standard      none        100   100   100   100,0
115   female   group E   bachelor's degree              standard      completed   99    100   100   99,7
180   female   group D   some high school               standard      completed   97    100   100   99,0
...
```

---

## Key Design Decisions

- **`PreparedStatement` everywhere** — no string concatenation in SQL, no SQL-injection risk.
- **`try-with-resources`** on every `Connection`, `PreparedStatement`, and `ResultSet`.
- **Batch insert** (100 rows per `executeBatch`) with a single transaction; `rollback()` on any error.
- **Import guard** — `COUNT(*)` check prevents duplicate imports on repeated runs.
- **`Connection` injected into `StudentDaoImpl`** — caller controls transaction scope; no hidden connection management inside the DAO.
