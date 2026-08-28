package serangooner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

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
    public void showFarewell_always_saysByeThenDivider() {
        uiReading("").showFarewell();
        assertEquals("bye~" + System.lineSeparator() + DIVIDER + System.lineSeparator(),
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void showMessage_anyMessage_printsItAlone() {
        uiReading("").showMessage("added todo: [T][ ] read book");
        assertEquals("added todo: [T][ ] read book" + System.lineSeparator(),
                output.toString(StandardCharsets.UTF_8));
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
}
