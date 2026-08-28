package serangooner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

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
    public void addTodo_todo_addsTaskAndCountsIt() {
        TaskList tasks = new TaskList();
        String message = tasks.addTodo(new Todo("read book"));
        assertEquals(1, tasks.size());
        assertTrue(message.contains("[T][ ] read book"));
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
    }

    @Test
    public void unmark_markedTask_marksTaskIncomplete() {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        tasks.mark(1);
        tasks.unmark(1);
        assertFalse(tasks.getTasks().get(0).isDone());
    }

    @Test
    public void delete_validNumber_removesTask() {
        TaskList tasks = new TaskList(List.of(new Todo("read book"), new Todo("write essay")));
        tasks.delete(1);
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
    public void occurringOn_singleDate_listsOnlyMatchingTasks() {
        TaskList tasks = new TaskList(List.of(
                new Todo("read book"),
                new Deadline("submit ip", "2026-09-01"),
                new Deadline("submit tp", "2026-09-02")));

        String listing = tasks.occurringOn(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 1));

        assertTrue(listing.contains("submit ip"));
        assertFalse(listing.contains("submit tp"));
        assertFalse(listing.contains("read book"));
    }

    @Test
    public void occurringOn_range_keepsNumbersFromTheFullList() {
        TaskList tasks = new TaskList(List.of(
                new Todo("read book"),
                new Deadline("submit ip", "2026-09-01"),
                new Deadline("submit tp", "2026-09-02")));

        String listing = tasks.occurringOn(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 2));

        assertTrue(listing.contains(" 2. [D][ ] submit ip (by: 01 Sep 2026)"));
        assertTrue(listing.contains(" 3. [D][ ] submit tp (by: 02 Sep 2026)"));
    }

    @Test
    public void occurringOn_noMatchingTask_saysSo() {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        assertTrue(tasks.occurringOn(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 1))
                .contains("you have nothing"));
    }
}
