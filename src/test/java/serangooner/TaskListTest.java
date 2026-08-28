package serangooner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void addTodo_validCommand_addsTask() {
        TaskList tasks = new TaskList();
        String message = tasks.addTodo("todo read book");
        assertEquals(1, tasks.size());
        assertTrue(message.contains("[T][ ] read book"));
    }

    @Test
    public void addTodo_noDescription_exceptionThrown() {
        TaskList tasks = new TaskList();
        assertThrows(SerangoonerException.class, () -> tasks.addTodo("todo"));
        assertEquals(0, tasks.size());
    }

    @Test
    public void mark_validNumber_marksTaskDone() {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        tasks.mark("mark 1");
        assertTrue(tasks.getTasks().get(0).isDone());
    }

    @Test
    public void mark_numberOutOfRange_exceptionThrown() {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        assertThrows(SerangoonerException.class, () -> tasks.mark("mark 2"));
    }

    @Test
    public void delete_validNumber_removesTask() {
        TaskList tasks = new TaskList(List.of(new Todo("read book"), new Todo("write essay")));
        tasks.delete("delete 1");
        assertEquals(1, tasks.size());
        assertEquals("[T][ ] write essay", tasks.getTasks().get(0).toString());
    }

    @Test
    public void undo_afterDelete_putsTaskBackInPlace() {
        TaskList tasks = new TaskList(List.of(new Todo("read book"), new Todo("write essay")));
        tasks.delete("delete 1");
        assertTrue(tasks.undo());
        assertEquals(2, tasks.size());
        assertEquals("[T][ ] read book", tasks.getTasks().get(0).toString());
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

        String listing = tasks.occurringOn("on 2026-09-01");

        assertTrue(listing.contains("submit ip"));
        assertFalse(listing.contains("submit tp"));
        assertFalse(listing.contains("read book"));
    }

    @Test
    public void occurringOn_rangeEndingBeforeItStarts_exceptionThrown() {
        TaskList tasks = new TaskList();
        assertThrows(SerangoonerException.class,
                () -> tasks.occurringOn("on 2026-09-05 to 2026-09-01"));
    }
}
