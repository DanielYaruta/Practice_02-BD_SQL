package org.example.model;

public class Student {

    private final int    id;
    private final String gender;
    private final String raceEthnicity;
    private final String parentalLevelOfEducation;
    private final String lunch;
    private final String testPreparationCourse;
    private final int    mathScore;
    private final int    readingScore;
    private final int    writingScore;

    public Student(int id, String gender, String raceEthnicity,
                   String parentalLevelOfEducation, String lunch,
                   String testPreparationCourse,
                   int mathScore, int readingScore, int writingScore) {
        this.id                        = id;
        this.gender                    = gender;
        this.raceEthnicity             = raceEthnicity;
        this.parentalLevelOfEducation  = parentalLevelOfEducation;
        this.lunch                     = lunch;
        this.testPreparationCourse     = testPreparationCourse;
        this.mathScore                 = mathScore;
        this.readingScore              = readingScore;
        this.writingScore              = writingScore;
    }

    public int    getId()                       { return id; }
    public String getGender()                   { return gender; }
    public String getRaceEthnicity()            { return raceEthnicity; }
    public String getParentalLevelOfEducation() { return parentalLevelOfEducation; }
    public String getLunch()                    { return lunch; }
    public String getTestPreparationCourse()    { return testPreparationCourse; }
    public int    getMathScore()                { return mathScore; }
    public int    getReadingScore()             { return readingScore; }
    public int    getWritingScore()             { return writingScore; }

    public double getAverageScore() {
        return (mathScore + readingScore + writingScore) / 3.0;
    }

    @Override
    public String toString() {
        return String.format(
            "Student{id=%-4d gender=%-7s race=%-9s edu=%-30s lunch=%-13s prep=%-10s math=%-3d read=%-3d write=%-3d avg=%.1f}",
            id, gender, raceEthnicity, parentalLevelOfEducation,
            lunch, testPreparationCourse, mathScore, readingScore, writingScore, getAverageScore()
        );
    }
}
