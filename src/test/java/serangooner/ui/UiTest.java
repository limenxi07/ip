package serangooner.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import serangooner.storage.Storage;
import serangooner.task.Deadline;
import serangooner.task.Event;
import serangooner.task.Task;
import serangooner.task.TaskDateTime;
import serangooner.task.TaskList;
import serangooner.task.Todo;

public class UiTest {
    private static final String DIVIDER = "━━━ . °‧ 𓆝 𓆟 𓆞 ·｡";

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private Ui uiReading(String input) {
        return new Ui(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(output, true, StandardCharsets.UTF_8));
    }

    private static List<Task> threeTasks() {
        return List.of(new Todo("read book"), new Todo("write essay"), new Todo("nap"));
    }

    private List<String> printedLines() {
        String printed = output.toString(StandardCharsets.UTF_8);
        return printed.isEmpty() ? List.of() : List.of(printed.split(System.lineSeparator(), -1));
    }

    @Test
    public void showWelcome_always_greetsBetweenDividers() {
        uiReading("").showWelcome();
        List<String> lines = printedLines();
        assertEquals(DIVIDER, lines.get(lines.size() - 2));
        assertTrue(lines.contains("serangooner at your service. what's up?"));
        assertTrue(lines.contains("to see commands, type 'help'"));
        assertTrue(lines.contains("done? type 'bye' to exit :("));
    }

    @Test
    public void showFarewell_always_saysByeAloneAndLeavesTheDividerToTheLoop() {
        uiReading("").showFarewell();
        assertEquals("bye~" + System.lineSeparator(), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void showHelp_severalCommands_numbersThemAndEndsWithTheDateHint() {
        List<String> commands = List.of("todo <description> - add a task",
                "list - view all saved tasks", "bye - exit serangooner");

        uiReading("").showHelp(commands);

        List<String> lines = printedLines();
        assertEquals("serangooner commands:", lines.get(0));
        assertEquals("1. todo <description> - add a task", lines.get(1));
        assertEquals("3. bye - exit serangooner", lines.get(3));
        assertEquals(TaskDateTime.FORMAT_HINT, lines.get(commands.size() + 1));
    }

    @Test
    public void showHelp_noCommands_printsTheHeadingAndTheDateHintAlone() {
        uiReading("").showHelp(List.of());

        List<String> lines = printedLines();
        assertEquals("serangooner commands:", lines.get(0));
        assertEquals(TaskDateTime.FORMAT_HINT, lines.get(1));
    }

    @Test
    public void showError_anyMessage_printsItAlone() {
        uiReading("").showError("invalid command :/");
        assertEquals("invalid command :/" + System.lineSeparator(),
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void showLoadReport_nothingLoaded_printsNothing() {
        uiReading("").showLoadReport(new Storage.LoadResult(List.of(), 0, ""));
        assertEquals("", output.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void showLoadReport_tasksLoaded_reportsCount() {
        uiReading("").showLoadReport(new Storage.LoadResult(threeTasks(), 0, ""));
        assertEquals("loaded 3 task(s) from your last visit" + System.lineSeparator()
                + DIVIDER + System.lineSeparator(), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void showLoadReport_linesSkipped_reportsBothCounts() {
        uiReading("").showLoadReport(new Storage.LoadResult(threeTasks(), 2, ""));
        assertEquals("loaded 3 task(s) from your last visit; skipped 2 unreadable line(s)"
                + System.lineSeparator() + DIVIDER + System.lineSeparator(),
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void showLoadReport_loadFailed_reportsReason() {
        uiReading("").showLoadReport(new Storage.LoadResult(List.of(), 0, "couldn't read it"));
        assertEquals("couldn't read it" + System.lineSeparator() + DIVIDER + System.lineSeparator(),
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void readCommand_severalLines_returnsThemInTurn() {
        Ui ui = uiReading("list" + System.lineSeparator() + "bye" + System.lineSeparator());
        assertTrue(ui.hasNextCommand());
        assertEquals("list", ui.readCommand());
        assertEquals("bye", ui.readCommand());
        assertFalse(ui.hasNextCommand());
    }

    @Test
    public void hasNextCommand_noInput_returnsFalse() {
        assertFalse(uiReading("").hasNextCommand());
    }

    @Test
    public void showTaskAdded_task_namesItsKindAndTheCount() {
        uiReading("").showTaskAdded(new Todo("read book"), 3);
        assertEquals("added todo: [T][ ] read book" + System.lineSeparator()
                + "you now have 3 pending task(s) :c" + System.lineSeparator(),
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void showTaskAdded_deadline_callsItADeadline() {
        uiReading("").showTaskAdded(new Deadline("submit ip", "2026-09-01"), 1);
        assertTrue(output.toString(StandardCharsets.UTF_8).startsWith("added deadline: "));
    }

    @Test
    public void showTaskMarked_task_saysItIsDone() {
        uiReading("").showTaskMarked(new Todo("read book"));
        assertEquals("marked task as done: [T][ ] read book" + System.lineSeparator(),
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void showTaskDeleted_task_saysItIsGone() {
        uiReading("").showTaskDeleted(new Todo("read book"));
        assertEquals("deleted task: [T][ ] read book" + System.lineSeparator(),
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void showUndo_nothingToUndo_saysSo() {
        uiReading("").showUndo(false);
        assertEquals("there's nothing to undo >:(" + System.lineSeparator(),
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void showTasks_severalTasks_numbersThemUnderAHeading() {
        uiReading("").showTasks(new TaskList(threeTasks()).entries());
        List<String> lines = printedLines();
        assertEquals("your list", lines.get(0));
        assertEquals(" 1. [T][ ] read book", lines.get(1));
        assertEquals(" 3. [T][ ] nap", lines.get(3));
    }

    @Test
    public void showTasks_emptyList_saysSo() {
        uiReading("").showTasks(new TaskList().entries());
        assertTrue(output.toString(StandardCharsets.UTF_8).startsWith("your list is empty"));
    }

    @Test
    public void showTasksInRange_singleDate_headsTheListingWithThatDay() {
        TaskList tasks = new TaskList(List.of(new Deadline("submit ip", "2026-09-01")));
        LocalDate date = LocalDate.of(2026, 9, 1);

        uiReading("").showTasksInRange(tasks.occurringOn(date, date), date, date);

        assertEquals("tasks on 01 Sep 2026", printedLines().get(0));
    }

    @Test
    public void showTasksInRange_span_headsTheListingWithBothEnds() {
        TaskList tasks = new TaskList(List.of(new Deadline("submit ip", "2026-09-01")));
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 9, 4);

        uiReading("").showTasksInRange(tasks.occurringOn(start, end), start, end);

        assertEquals("tasks between 01 Sep 2026 and 04 Sep 2026", printedLines().get(0));
    }

    @Test
    public void showTasksInRange_noMatchingTask_saysSo() {
        LocalDate date = LocalDate.of(2026, 9, 1);
        uiReading("").showTasksInRange(new TaskList().occurringOn(date, date), date, date);
        assertEquals("you have nothing on 01 Sep 2026 :D" + System.lineSeparator(),
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void showTaskUnmarked_task_saysItIsIncomplete() {
        Task task = new Todo("read book");
        task.markDone();

        uiReading("").showTaskUnmarked(task);

        assertEquals(List.of("marked task as incomplete: [T][\u2713] read book", ""),
                printedLines());
    }

    @Test
    public void showUndo_editUndone_saysSo() {
        uiReading("").showUndo(true);
        assertEquals(List.of("undid your last edit", ""), printedLines());
    }

    @Test
    public void showDivider_always_drawsTheDividerAlone() {
        uiReading("").showDivider();
        assertEquals(List.of(DIVIDER, ""), printedLines());
    }

    @Test
    public void hasNextCommand_inputAvailable_returnsTrue() {
        assertTrue(uiReading("list" + System.lineSeparator()).hasNextCommand());
    }

    @Test
    public void hasNextCommand_lastLineAlreadyRead_returnsFalse() {
        Ui ui = uiReading("list" + System.lineSeparator());
        ui.readCommand();
        assertFalse(ui.hasNextCommand());
    }

    @Test
    public void readCommand_blankLine_returnsItUnchanged() {
        assertEquals("", uiReading(System.lineSeparator()).readCommand());
    }

    @Test
    public void readCommand_lineWithSurroundingSpaces_leavesThemForTheParser() {
        assertEquals("  list  ", uiReading("  list  " + System.lineSeparator()).readCommand());
    }

    @Test
    public void showTaskAdded_event_callsItAnEvent() {
        uiReading("").showTaskAdded(new Event("demo", "2026-09-05", "2026-09-06"), 1);
        assertTrue(printedLines().get(0).startsWith("added event: "));
    }
}
