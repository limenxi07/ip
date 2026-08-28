package serangooner.command;

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

import serangooner.Parser;
import serangooner.SerangoonerException;
import serangooner.storage.Storage;
import serangooner.task.Deadline;
import serangooner.task.TaskList;
import serangooner.task.Todo;
import serangooner.ui.Ui;

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
        for (CommandType command : CommandType.values()) {
            assertTrue(printed().contains(command.getSyntax() + " - " + command.getDescription()));
        }
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


    @Test
    public void execute_addCommandOnNonEmptyList_appendsAndReportsTheNewCount(
            @TempDir Path directory) {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        Storage storage = storageIn(directory);

        new AddCommand(new Deadline("submit ip", "2026-09-01")).execute(tasks, ui(), storage);

        assertEquals(2, tasks.size());
        assertEquals("[D][ ] submit ip (by: 01 Sep 2026)",
                tasks.getTasks().get(1).toString());
        assertTrue(printed().contains("you now have 2 pending task(s)"));
    }

    @Test
    public void execute_markCommand_showsTheMarkedTask(@TempDir Path directory) {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));

        new MarkCommand(1).execute(tasks, ui(), storageIn(directory));

        assertTrue(printed().startsWith("marked task as done: [T][\u2713] read book"));
    }

    @Test
    public void execute_unmarkCommand_showsTheTaskAndSavesIt(@TempDir Path directory) {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        Storage storage = storageIn(directory);
        new MarkCommand(1).execute(tasks, ui(), storage);
        output.reset();

        new UnmarkCommand(1).execute(tasks, ui(), storage);

        assertTrue(printed().startsWith("marked task as incomplete: [T][ ] read book"));
        assertFalse(storage.load().tasks().get(0).isDone());
    }

    @Test
    public void execute_unmarkCommandOutOfRange_exceptionThrownAndNothingSaved(
            @TempDir Path directory) {
        TaskList tasks = new TaskList();
        Storage storage = storageIn(directory);

        assertThrows(SerangoonerException.class,
                () -> new UnmarkCommand(1).execute(tasks, ui(), storage));
        assertTrue(storage.load().tasks().isEmpty());
    }

    @Test
    public void execute_deleteCommandOutOfRange_exceptionThrownAndListUntouched(
            @TempDir Path directory) {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        Storage storage = storageIn(directory);

        assertThrows(SerangoonerException.class,
                () -> new DeleteCommand(2).execute(tasks, ui(), storage));
        assertEquals(1, tasks.size());
    }

    @Test
    public void execute_undoCommandAfterMark_leavesTheTaskIncompleteAndSavesIt(
            @TempDir Path directory) {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        Storage storage = storageIn(directory);
        new MarkCommand(1).execute(tasks, ui(), storage);

        new UndoCommand().execute(tasks, ui(), storage);

        assertFalse(tasks.getTasks().get(0).isDone());
        assertFalse(storage.load().tasks().get(0).isDone());
    }

    @Test
    public void execute_undoCommandAfterDelete_putsTheTaskBackAndSavesIt(
            @TempDir Path directory) {
        TaskList tasks = new TaskList(List.of(new Todo("read book"), new Todo("write essay")));
        Storage storage = storageIn(directory);
        new DeleteCommand(1).execute(tasks, ui(), storage);

        new UndoCommand().execute(tasks, ui(), storage);

        assertEquals(2, tasks.size());
        assertEquals("[T][ ] read book", tasks.getTasks().get(0).toString());
        assertEquals(2, storage.load().tasks().size());
    }

    @Test
    public void execute_listCommandOnEmptyList_saysThereIsNothing(@TempDir Path directory) {
        new ListCommand().execute(new TaskList(), ui(), storageIn(directory));
        assertFalse(printed().isBlank());
        assertFalse(printed().contains(" 1. "));
    }

    @Test
    public void execute_onCommandMatchingNothing_saysSo(@TempDir Path directory) {
        TaskList tasks = new TaskList(List.of(new Deadline("submit ip", "2026-09-01")));

        new OnCommand(Parser.parseDateRange("on 2026-10-01")).execute(tasks, ui(),
                storageIn(directory));

        assertFalse(printed().contains("submit ip"));
    }

    @Test
    public void execute_onCommandOverASpan_showsEveryTaskInIt(@TempDir Path directory) {
        TaskList tasks = new TaskList(List.of(
                new Deadline("submit ip", "2026-09-01"),
                new Deadline("submit tp", "2026-09-09"),
                new Deadline("submit cp", "2026-10-01")));

        new OnCommand(Parser.parseDateRange("on 2026-09-01 to 2026-09-09"))
                .execute(tasks, ui(), storageIn(directory));

        assertTrue(printed().contains("submit ip"));
        assertTrue(printed().contains("submit tp"));
        assertFalse(printed().contains("submit cp"));
    }

    @Test
    public void execute_readOnlyCommands_leaveTheSaveFileAlone(@TempDir Path directory) {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));
        Storage storage = storageIn(directory);

        new ListCommand().execute(tasks, ui(), storage);
        new HelpCommand().execute(tasks, ui(), storage);
        new ExitCommand().execute(tasks, ui(), storage);

        assertTrue(storage.load().tasks().isEmpty());
    }
}
