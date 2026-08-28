package serangooner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CommandTest {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private Ui ui() {
        return new Ui(new ByteArrayInputStream(new byte[0]),
                new PrintStream(output, true, StandardCharsets.UTF_8));
    }

    private static Storage storageIn(Path directory) {
        return new Storage(directory.resolve("tasks.txt"));
    }

    private String printed() {
        return output.toString(StandardCharsets.UTF_8);
    }

    @Test
    public void execute_addCommand_addsTaskShowsItAndSavesIt(@TempDir Path directory) {
        TaskList tasks = new TaskList();
        Storage storage = storageIn(directory);

        new AddCommand(new Todo("read book")).execute(tasks, ui(), storage);

        assertEquals(1, tasks.size());
        assertTrue(printed().startsWith("added todo: [T][ ] read book"));
        assertEquals(1, storage.load().tasks().size());
    }

    @Test
    public void execute_markCommand_marksTaskAndSavesIt(@TempDir Path directory) {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        Storage storage = storageIn(directory);

        new MarkCommand(1).execute(tasks, ui(), storage);

        assertTrue(tasks.getTasks().get(0).isDone());
        assertTrue(storage.load().tasks().get(0).isDone());
    }

    @Test
    public void execute_unmarkCommand_marksTaskIncomplete(@TempDir Path directory) {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        Storage storage = storageIn(directory);
        new MarkCommand(1).execute(tasks, ui(), storage);

        new UnmarkCommand(1).execute(tasks, ui(), storage);

        assertFalse(tasks.getTasks().get(0).isDone());
    }

    @Test
    public void execute_markCommandOutOfRange_exceptionThrownAndNothingSaved(
            @TempDir Path directory) {
        TaskList tasks = new TaskList();
        Storage storage = storageIn(directory);

        assertThrows(SerangoonerException.class,
                () -> new MarkCommand(1).execute(tasks, ui(), storage));
        assertTrue(storage.load().tasks().isEmpty());
    }

    @Test
    public void execute_deleteCommand_removesTaskAndSavesIt(@TempDir Path directory) {
        TaskList tasks = new TaskList(List.of(new Todo("read book"), new Todo("write essay")));
        Storage storage = storageIn(directory);

        new DeleteCommand(1).execute(tasks, ui(), storage);

        assertEquals(1, tasks.size());
        assertTrue(printed().startsWith("deleted task: [T][ ] read book"));
        assertEquals(1, storage.load().tasks().size());
    }

    @Test
    public void execute_undoCommand_reversesTheLastChange(@TempDir Path directory) {
        TaskList tasks = new TaskList();
        Storage storage = storageIn(directory);
        new AddCommand(new Todo("read book")).execute(tasks, ui(), storage);

        new UndoCommand().execute(tasks, ui(), storage);

        assertEquals(0, tasks.size());
        assertTrue(storage.load().tasks().isEmpty());
    }

    @Test
    public void execute_undoCommandWithNothingToUndo_saysSo(@TempDir Path directory) {
        new UndoCommand().execute(new TaskList(), ui(), storageIn(directory));
        assertTrue(printed().startsWith("there's nothing to undo"));
    }

    @Test
    public void execute_listCommand_showsEveryTask(@TempDir Path directory) {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));

        new ListCommand().execute(tasks, ui(), storageIn(directory));

        assertTrue(printed().contains(" 1. [T][ ] read book"));
    }

    @Test
    public void execute_onCommand_showsOnlyTasksInTheRange(@TempDir Path directory) {
        TaskList tasks = new TaskList(List.of(
                new Deadline("submit ip", "2026-09-01"),
                new Deadline("submit tp", "2026-09-09")));

        new OnCommand(Parser.parseDateRange("on 2026-09-01")).execute(tasks, ui(),
                storageIn(directory));

        assertTrue(printed().contains("submit ip"));
        assertFalse(printed().contains("submit tp"));
    }

    @Test
    public void execute_helpCommand_listsEveryCommand(@TempDir Path directory) {
        new HelpCommand().execute(new TaskList(), ui(), storageIn(directory));
        assertTrue(printed().startsWith("serangooner commands:"));
    }

    @Test
    public void execute_exitCommand_saysBye(@TempDir Path directory) {
        new ExitCommand().execute(new TaskList(), ui(), storageIn(directory));
        assertEquals("bye~" + System.lineSeparator(), printed());
    }

    @Test
    public void isExit_exitCommand_returnsTrue() {
        assertTrue(new ExitCommand().isExit());
    }

    @Test
    public void isExit_anyOtherCommand_returnsFalse() {
        assertFalse(new ListCommand().isExit());
        assertFalse(new AddCommand(new Todo("read book")).isExit());
        assertFalse(new UndoCommand().isExit());
    }
}
