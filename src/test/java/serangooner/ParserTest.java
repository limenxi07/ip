package serangooner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class ParserTest {
    @Test
    public void parseCommandType_knownKeyword_returnsThatCommand() {
        assertEquals(CommandType.TODO, Parser.parseCommandType("todo read book"));
        assertEquals(CommandType.LIST, Parser.parseCommandType("list"));
    }

    @Test
    public void parseCommandType_unknownKeyword_exceptionThrown() {
        assertThrows(SerangoonerException.class, () -> Parser.parseCommandType("blah"));
    }

    @Test
    public void parseCommandType_blankInput_exceptionThrown() {
        assertThrows(SerangoonerException.class, () -> Parser.parseCommandType("   "));
    }

    @Test
    public void parseTodo_validCommand_returnsTodo() {
        assertEquals("[T][ ] read book", Parser.parseTodo("todo read book").toString());
    }

    @Test
    public void parseTodo_noDescription_exceptionThrown() {
        assertThrows(SerangoonerException.class, () -> Parser.parseTodo("todo"));
        assertThrows(SerangoonerException.class, () -> Parser.parseTodo("todo   "));
    }

    @Test
    public void parseTodo_leadingSpace_ignoresIt() {
        assertEquals("[T][ ] read book", Parser.parseTodo("  todo read book").toString());
    }

    @Test
    public void parseDeadline_validCommand_returnsDeadline() {
        assertEquals("[D][ ] submit ip (by: 01 Sep 2026)",
                Parser.parseDeadline("deadline submit ip by 2026-09-01").toString());
    }

    @Test
    public void parseDeadline_dateWithTime_keepsTimeOfDay() {
        assertEquals("[D][ ] submit ip (by: 01 Sep 2026, 6:00PM)",
                Parser.parseDeadline("deadline submit ip by 2026-09-01 1800").toString());
    }

    @Test
    public void parseDeadline_missingParts_exceptionThrown() {
        assertThrows(SerangoonerException.class, () -> Parser.parseDeadline("deadline submit ip"));
        assertThrows(SerangoonerException.class, () -> Parser.parseDeadline("deadline submit ip by "));
        assertThrows(SerangoonerException.class, () -> Parser.parseDeadline("deadline by 2026-09-01"));
    }

    @Test
    public void parseDeadline_unreadableDate_exceptionThrown() {
        assertThrows(SerangoonerException.class,
                () -> Parser.parseDeadline("deadline submit ip by tomorrow"));
    }

    @Test
    public void parseEvent_validCommand_returnsEvent() {
        assertEquals("[E][ ] orbital (from: 03 Sep 2026 to: 04 Sep 2026)",
                Parser.parseEvent("event orbital from 2026-09-03 to 2026-09-04").toString());
    }

    @Test
    public void parseEvent_leadingSpace_ignoresIt() {
        assertEquals("[E][ ] orbital (from: 03 Sep 2026 to: 04 Sep 2026)",
                Parser.parseEvent("   event orbital from 2026-09-03 to 2026-09-04").toString());
    }

    @Test
    public void parseEvent_missingParts_exceptionThrown() {
        assertThrows(SerangoonerException.class,
                () -> Parser.parseEvent("event orbital from 2026-09-03"));
        assertThrows(SerangoonerException.class,
                () -> Parser.parseEvent("event orbital to 2026-09-04"));
        assertThrows(SerangoonerException.class,
                () -> Parser.parseEvent("event from 2026-09-03 to 2026-09-04"));
    }

    @Test
    public void parseEvent_endBeforeStart_exceptionThrown() {
        assertThrows(SerangoonerException.class,
                () -> Parser.parseEvent("event orbital from 2026-09-05 to 2026-09-01"));
    }

    @Test
    public void parseTaskNumber_validNumber_returnsIt() {
        assertEquals(3, Parser.parseTaskNumber("mark 3", CommandType.MARK));
        assertEquals(1, Parser.parseTaskNumber("delete   1  ", CommandType.DELETE));
    }

    @Test
    public void parseTaskNumber_leadingSpace_ignoresIt() {
        assertEquals(2, Parser.parseTaskNumber("  mark 2", CommandType.MARK));
    }

    @Test
    public void parseTaskNumber_numberNamingNoTask_returnsIt() {
        // Whether a number names a task depends on the list, so the parser lets it through.
        assertEquals(99, Parser.parseTaskNumber("unmark 99", CommandType.UNMARK));
    }

    @Test
    public void parseTaskNumber_missingNumber_exceptionThrown() {
        assertThrows(SerangoonerException.class,
                () -> Parser.parseTaskNumber("mark", CommandType.MARK));
    }

    @Test
    public void parseTaskNumber_notANumber_exceptionThrown() {
        assertThrows(SerangoonerException.class,
                () -> Parser.parseTaskNumber("mark abc", CommandType.MARK));
    }

    @Test
    public void parseDateRange_singleDate_coversThatDayAlone() {
        Parser.DateRange range = Parser.parseDateRange("on 2026-09-01");
        assertEquals(LocalDate.of(2026, 9, 1), range.start());
        assertEquals(LocalDate.of(2026, 9, 1), range.end());
    }

    @Test
    public void parseDateRange_dateWithTime_keepsTheDateAlone() {
        Parser.DateRange range = Parser.parseDateRange("on 2026-09-01 1800");
        assertEquals(LocalDate.of(2026, 9, 1), range.start());
        assertEquals(LocalDate.of(2026, 9, 1), range.end());
    }

    @Test
    public void parseDateRange_twoDates_coversBothEnds() {
        Parser.DateRange range = Parser.parseDateRange("on 2026-09-01 to 2026-09-04");
        assertEquals(LocalDate.of(2026, 9, 1), range.start());
        assertEquals(LocalDate.of(2026, 9, 4), range.end());
    }

    @Test
    public void parseDateRange_noDate_exceptionThrown() {
        assertThrows(SerangoonerException.class, () -> Parser.parseDateRange("on"));
    }

    @Test
    public void parseDateRange_unreadableDate_exceptionThrown() {
        assertThrows(SerangoonerException.class, () -> Parser.parseDateRange("on someday"));
    }

    @Test
    public void parseDateRange_endBeforeStart_exceptionThrown() {
        assertThrows(SerangoonerException.class,
                () -> Parser.parseDateRange("on 2026-09-05 to 2026-09-01"));
    }
}
