package serangooner.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import serangooner.SerangoonerException;

public class TaskListTest {
    @Test
    public void constructor_noArguments_startsEmpty() {
        assertEquals(0, new TaskList().size());
    }

    @Test
    public void constructor_givenTasks_holdsThemInOrder() {
        TaskList tasks = new TaskList(List.of(new Todo("read book"), new Todo("write essay")));
        assertEquals(2, tasks.size());
        assertEquals("[T][ ] read book", tasks.getTasks().get(0).toString());
        assertEquals("[T][ ] write essay", tasks.getTasks().get(1).toString());
    }

    @Test
    public void getTasks_always_cannotBeEditedThrough() {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        assertThrows(UnsupportedOperationException.class,
                () -> tasks.getTasks().add(new Todo("sneak in")));
    }

    @Test
    public void add_task_appendsItAndReturnsIt() {
        TaskList tasks = new TaskList();
        Todo todo = new Todo("read book");
        assertSame(todo, tasks.add(todo));
        assertEquals(1, tasks.size());
        assertSame(todo, tasks.getTasks().get(0));
    }

    @Test
    public void mark_validNumber_marksTaskDone() {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        tasks.mark(1);
        assertTrue(tasks.getTasks().get(0).isDone());
    }

    @Test
    public void mark_numberOutOfRange_exceptionThrown() {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        assertThrows(SerangoonerException.class, () -> tasks.mark(2));
        assertThrows(SerangoonerException.class, () -> tasks.mark(0));
    }

    @Test
    public void unmark_markedTask_marksTaskIncomplete() {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        tasks.mark(1);
        tasks.unmark(1);
        assertFalse(tasks.getTasks().get(0).isDone());
    }

    @Test
    public void delete_validNumber_removesTaskAndReturnsIt() {
        TaskList tasks = new TaskList(List.of(new Todo("read book"), new Todo("write essay")));
        assertEquals("[T][ ] read book", tasks.delete(1).toString());
        assertEquals(1, tasks.size());
        assertEquals("[T][ ] write essay", tasks.getTasks().get(0).toString());
    }

    @Test
    public void delete_emptyList_exceptionThrown() {
        assertThrows(SerangoonerException.class, () -> new TaskList().delete(1));
    }

    @Test
    public void undo_afterDelete_putsTaskBackInPlace() {
        TaskList tasks = new TaskList(List.of(new Todo("read book"), new Todo("write essay")));
        tasks.delete(1);
        assertTrue(tasks.undo());
        assertEquals(2, tasks.size());
        assertEquals("[T][ ] read book", tasks.getTasks().get(0).toString());
    }

    @Test
    public void undo_afterAdd_removesTheTask() {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));
        assertTrue(tasks.undo());
        assertEquals(0, tasks.size());
    }

    @Test
    public void undo_afterMark_leavesTaskIncomplete() {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        tasks.mark(1);
        assertTrue(tasks.undo());
        assertFalse(tasks.getTasks().get(0).isDone());
    }

    @Test
    public void undo_nothingDoneYet_returnsFalse() {
        assertFalse(new TaskList().undo());
    }

    @Test
    public void entries_always_numbersFromOne() {
        TaskList tasks = new TaskList(List.of(new Todo("read book"), new Todo("write essay")));

        List<TaskList.Entry> entries = tasks.entries();

        assertEquals(2, entries.size());
        assertEquals(1, entries.get(0).number());
        assertEquals(2, entries.get(1).number());
        assertEquals("[T][ ] write essay", entries.get(1).task().toString());
    }

    @Test
    public void occurringOn_singleDate_returnsOnlyMatchingTasks() {
        TaskList tasks = new TaskList(List.of(
                new Todo("read book"),
                new Deadline("submit ip", "2026-09-01"),
                new Deadline("submit tp", "2026-09-02")));

        List<TaskList.Entry> entries =
                tasks.occurringOn(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 1));

        assertEquals(1, entries.size());
        assertEquals("[D][ ] submit ip (by: 01 Sep 2026)", entries.get(0).task().toString());
    }

    @Test
    public void occurringOn_range_keepsNumbersFromTheFullList() {
        TaskList tasks = new TaskList(List.of(
                new Todo("read book"),
                new Deadline("submit ip", "2026-09-01"),
                new Deadline("submit tp", "2026-09-02")));

        List<TaskList.Entry> entries =
                tasks.occurringOn(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 2));

        assertEquals(2, entries.size());
        assertEquals(2, entries.get(0).number());
        assertEquals(3, entries.get(1).number());
    }

    @Test
    public void occurringOn_noMatchingTask_returnsNothing() {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        assertTrue(tasks.occurringOn(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 1)).isEmpty());
    }

    @Test
    public void undo_afterUnmark_leavesTaskDoneAgain() {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        tasks.mark(1);
        tasks.unmark(1);

        assertTrue(tasks.undo());

        assertTrue(tasks.getTasks().get(0).isDone());
    }

    @Test
    public void undo_severalEdits_reversesThemMostRecentFirst() {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));
        tasks.add(new Todo("write essay"));
        tasks.delete(1);

        assertTrue(tasks.undo());
        assertEquals(2, tasks.size());
        assertEquals("[T][ ] read book", tasks.getTasks().get(0).toString());

        assertTrue(tasks.undo());
        assertEquals(1, tasks.size());
        assertEquals("[T][ ] read book", tasks.getTasks().get(0).toString());
    }

    @Test
    public void undo_everyEditUndone_returnsFalseOnceMore() {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));

        assertTrue(tasks.undo());
        assertFalse(tasks.undo());
    }

    @Test
    public void occurringOn_rangeEndingOnTheTaskDate_includesIt() {
        TaskList tasks = new TaskList(List.of(new Deadline("submit ip", "2026-09-05")));
        assertEquals(1, tasks.occurringOn(LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 5)).size());
    }

    @Test
    public void occurringOn_rangeStartingOnTheTaskDate_includesIt() {
        TaskList tasks = new TaskList(List.of(new Deadline("submit ip", "2026-09-01")));
        assertEquals(1, tasks.occurringOn(LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 5)).size());
    }

    @Test
    public void mark_alreadyDoneTask_leavesItDone() {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        tasks.mark(1);
        tasks.mark(1);
        assertTrue(tasks.getTasks().get(0).isDone());
    }

    @Test
    public void delete_numberOutOfRange_exceptionThrown() {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        assertThrows(SerangoonerException.class, () -> tasks.delete(2));
        assertThrows(SerangoonerException.class, () -> tasks.delete(0));
    }

    @Test
    public void unmark_numberOutOfRange_exceptionThrown() {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        assertThrows(SerangoonerException.class, () -> tasks.unmark(2));
    }

    @Test
    public void size_afterAddAndDelete_tracksTheCount() {
        TaskList tasks = new TaskList();
        assertEquals(0, tasks.size());
        tasks.add(new Todo("read book"));
        tasks.add(new Todo("write essay"));
        assertEquals(2, tasks.size());
        tasks.delete(1);
        assertEquals(1, tasks.size());
    }

    @Test
    public void entries_emptyList_returnsNothing() {
        assertEquals(List.of(), new TaskList().entries());
    }

    @Test
    public void entries_always_pairsEachNumberWithItsOwnTask() {
        TaskList tasks = new TaskList(List.of(new Todo("read book"), new Todo("write essay")));
        List<TaskList.Entry> entries = tasks.entries();
        assertEquals(2, entries.get(1).number());
        assertSame(tasks.getTasks().get(1), entries.get(1).task());
    }

    @Test
    public void mark_validNumber_returnsTheTaskItMarked() {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        assertSame(tasks.getTasks().get(0), tasks.mark(1));
    }

    @Test
    public void unmark_validNumber_returnsTheTaskItUnmarked() {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        tasks.mark(1);
        assertSame(tasks.getTasks().get(0), tasks.unmark(1));
    }

    @Test
    public void occurringOn_taskCarryingNoDate_leavesItOut() {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        assertEquals(List.of(),
                tasks.occurringOn(LocalDate.MIN, LocalDate.MAX));
    }
}
