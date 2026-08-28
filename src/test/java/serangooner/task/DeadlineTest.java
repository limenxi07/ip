package serangooner.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import serangooner.SerangoonerException;

public class DeadlineTest {
    @Test
    public void constructor_unreadableDate_exceptionThrown() {
        assertThrows(SerangoonerException.class, () -> new Deadline("submit ip", "next week"));
    }

    @Test
    public void toSaveFormat_bareDate_writesTheDateBackTheWayItWasTyped() {
        assertEquals("D | 0 | submit ip | 2026-09-01",
                new Deadline("submit ip", "2026-09-01").toSaveFormat());
    }

    @Test
    public void toSaveFormat_dateWithTime_keepsTheTimeOfDay() {
        assertEquals("D | 0 | submit ip | 2026-09-01 1800",
                new Deadline("submit ip", "2026-09-01 1800").toSaveFormat());
    }

    @Test
    public void toString_bareDate_showsTheDueDate() {
        assertEquals("[D][ ] submit ip (by: 01 Sep 2026)",
                new Deadline("submit ip", "2026-09-01").toString());
    }

    @Test
    public void toString_dateWithTime_showsTheTimeOfDay() {
        assertEquals("[D][ ] submit ip (by: 01 Sep 2026, 6:00PM)",
                new Deadline("submit ip", "2026-09-01 1800").toString());
    }

    @Test
    public void isWithin_rangeContainingTheDueDate_returnsTrue() {
        Task deadline = new Deadline("submit ip", "2026-09-03");
        assertTrue(deadline.isWithin(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5)));
    }

    @Test
    public void isWithin_rangeStartingOnTheDueDate_countsAsWithin() {
        Task deadline = new Deadline("submit ip", "2026-09-01");
        assertTrue(deadline.isWithin(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5)));
    }

    @Test
    public void isWithin_rangeEndingOnTheDueDate_countsAsWithin() {
        Task deadline = new Deadline("submit ip", "2026-09-05");
        assertTrue(deadline.isWithin(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5)));
    }

    @Test
    public void isWithin_dueDateWithATimeOfDay_ignoresTheTime() {
        Task deadline = new Deadline("submit ip", "2026-09-05 2359");
        assertTrue(deadline.isWithin(LocalDate.of(2026, 9, 5), LocalDate.of(2026, 9, 5)));
    }

    @Test
    public void isWithin_rangeEndingBeforeTheDueDate_returnsFalse() {
        Task deadline = new Deadline("submit ip", "2026-09-06");
        assertFalse(deadline.isWithin(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5)));
    }

    @Test
    public void isWithin_rangeStartingAfterTheDueDate_returnsFalse() {
        Task deadline = new Deadline("submit ip", "2026-08-31");
        assertFalse(deadline.isWithin(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5)));
    }

    @Test
    public void fromSaveFields_validLine_returnsDeadline() {
        Optional<Task> task = Deadline.fromSaveFields(
                new String[] {"D", "1", "submit ip", "2026-09-01 1800"});
        assertTrue(task.isPresent());
        assertTrue(task.get().isDone());
        assertEquals("[D][✓] submit ip (by: 01 Sep 2026, 6:00PM)", task.get().toString());
    }

    @Test
    public void fromSaveFields_unreadableDate_returnsNothing() {
        assertTrue(Deadline.fromSaveFields(
                new String[] {"D", "0", "submit ip", "not-a-date"}).isEmpty());
    }

    @Test
    public void fromSaveFields_missingDateField_returnsNothing() {
        assertTrue(Deadline.fromSaveFields(new String[] {"D", "0", "submit ip"}).isEmpty());
    }
}
