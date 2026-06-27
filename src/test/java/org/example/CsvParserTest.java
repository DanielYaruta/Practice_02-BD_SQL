package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CsvParser")
class CsvParserTest {

    @Test
    @DisplayName("parses standard quoted line into 8 fields")
    void parseLine_standardDataRow() {
        String line = "\"female\",\"group B\",\"bachelor's degree\",\"standard\",\"none\",\"72\",\"72\",\"74\"";
        String[] fields = CsvParser.parseLine(line);

        assertEquals(8, fields.length);
        assertEquals("female",             fields[0]);
        assertEquals("group B",            fields[1]);
        assertEquals("bachelor's degree",  fields[2]);
        assertEquals("standard",           fields[3]);
        assertEquals("none",               fields[4]);
        assertEquals("72",                 fields[5]);
        assertEquals("72",                 fields[6]);
        assertEquals("74",                 fields[7]);
    }

    @Test
    @DisplayName("parses the CSV header line correctly")
    void parseLine_headerRow() {
        String line = "\"gender\",\"race/ethnicity\",\"parental level of education\","
                    + "\"lunch\",\"test preparation course\","
                    + "\"math score\",\"reading score\",\"writing score\"";
        String[] fields = CsvParser.parseLine(line);

        assertEquals(8, fields.length);
        assertEquals("gender",                      fields[0]);
        assertEquals("race/ethnicity",              fields[1]);
        assertEquals("parental level of education", fields[2]);
        assertEquals("math score",                  fields[5]);
    }

    @Test
    @DisplayName("preserves apostrophe inside a quoted field")
    void parseLine_apostropheInField() {
        String line = "\"male\",\"group A\",\"master's degree\",\"standard\",\"completed\",\"80\",\"78\",\"81\"";
        String[] fields = CsvParser.parseLine(line);

        assertEquals("master's degree", fields[2]);
    }

    @Test
    @DisplayName("handles comma inside a quoted field without splitting")
    void parseLine_commaInsideQuotes() {
        String line = "\"Smith, John\",\"group C\",\"some college\",\"standard\",\"none\",\"70\",\"68\",\"65\"";
        String[] fields = CsvParser.parseLine(line);

        assertEquals(8, fields.length);
        assertEquals("Smith, John", fields[0]);
    }

    @Test
    @DisplayName("handles escaped double-quote (\"\") inside a quoted field")
    void parseLine_escapedDoubleQuote() {
        String line = "\"say \"\"hello\"\"\",\"group A\",\"high school\",\"standard\",\"none\",\"55\",\"60\",\"58\"";
        String[] fields = CsvParser.parseLine(line);

        assertEquals("say \"hello\"", fields[0]);
    }

    @Test
    @DisplayName("returns a single-element array for a plain value with no commas")
    void parseLine_singleField() {
        String[] fields = CsvParser.parseLine("\"hello\"");

        assertEquals(1, fields.length);
        assertEquals("hello", fields[0]);
    }

    @Test
    @DisplayName("handles an empty quoted field")
    void parseLine_emptyQuotedField() {
        String line = "\"female\",\"\",\"some college\",\"standard\",\"none\",\"70\",\"68\",\"65\"";
        String[] fields = CsvParser.parseLine(line);

        assertEquals(8, fields.length);
        assertEquals("", fields[1]);
    }
}
