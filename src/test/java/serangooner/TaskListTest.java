package serangooner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TaskListTest {
    @Test
    public void getLoadReport_noSaveFile_reportsNothing(@TempDir Path directory) {
        TaskList tasks = new TaskList(new Storage(directory.resolve("serangooner.txt")));
        assertEquals(new TaskList.LoadReport(0, 0, ""), tasks.getLoadReport());
        assertEquals(0, tasks.size());
    }

    @Test
    public void getLoadReport_readableAndUnreadableLines_countsBoth(@TempDir Path directory)
            throws IOException {
        Path file = directory.resolve("serangooner.txt");
        Files.write(file, java.util.List.of(
                "T | 0 | read book",
                "D | 0 | submit ip | 2026-09-01",
                "X | 1 | corrupted line",
                "bogus"));

        TaskList tasks = new TaskList(new Storage(file));
        assertEquals(new TaskList.LoadReport(2, 2, ""), tasks.getLoadReport());
        assertEquals(2, tasks.size());
    }
}
