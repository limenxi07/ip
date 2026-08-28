package serangooner.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import serangooner.SerangoonerException;
import serangooner.task.Deadline;
import serangooner.task.Event;
import serangooner.task.Task;
import serangooner.task.Todo;

public class StorageTest {
    @Test
    public void load_missingFile_returnsEmptyResult(@TempDir Path directory) {
        Storage.LoadResult result = new Storage(directory.resolve("absent.txt")).load();
        assertEquals(List.of(), result.tasks());
        assertEquals(0, result.skippedLineCount());
        assertEquals("", result.errorMessage());
    }

    @Test
    public void saveThenLoad_everyTaskType_readsBackTheSameList(@TempDir Path directory) {
        Storage storage = new Storage(directory.resolve("serangooner.txt"));
        Task todo = new Todo("read book");
        Task deadline = new Deadline("submit ip", "2026-09-01");
        Task event = new Event("orbital demo", "2026-09-05 1400", "2026-09-05 1600");
        deadline.markDone();

        storage.save(List.of(todo, deadline, event));
        List<Task> loaded = storage.load().tasks();

        assertEquals(3, loaded.size());
        assertEquals(todo.toString(), loaded.get(0).toString());
        assertEquals(deadline.toString(), loaded.get(1).toString());
        assertEquals(event.toString(), loaded.get(2).toString());
        assertTrue(loaded.get(1).isDone());
        assertFalse(loaded.get(0).isDone());
    }

    @Test
    public void save_missingParentDirectory_createsIt(@TempDir Path directory) {
        Path file = directory.resolve("data").resolve("serangooner.txt");
        new Storage(file).save(List.of(new Todo("read book")));
        assertTrue(Files.exists(file));
    }

    @Test
    public void save_emptyList_clearsTheFile(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("serangooner.txt");
        Files.write(file, List.of("T | 0 | read book"));

        new Storage(file).save(List.of());

        assertEquals(List.of(), Files.readAllLines(file));
    }

    @Test
    public void load_unreadableLines_skipsThemAndCountsThem(@TempDir Path directory)
            throws IOException {
        Path file = directory.resolve("serangooner.txt");
        Files.write(file, List.of(
                "T | 0 | read book",
                "X | 0 | unknown type code",
                "T | 7 | bad done flag",
                "T | 0",
                "D | 0 | no date given",
                "D | 0 | unparseable date | not-a-date",
                "E | 0 | ends first | 2026-09-05 | 2026-09-04",
                "bogus"));

        Storage.LoadResult result = new Storage(file).load();

        assertEquals(1, result.tasks().size());
        assertEquals(7, result.skippedLineCount());
        assertEquals("", result.errorMessage());
    }

    @Test
    public void load_blankLines_ignoresThemWithoutCounting(@TempDir Path directory)
            throws IOException {
        Path file = directory.resolve("serangooner.txt");
        Files.write(file, List.of("", "T | 0 | read book", "   ", ""));

        Storage.LoadResult result = new Storage(file).load();

        assertEquals(1, result.tasks().size());
        assertEquals(0, result.skippedLineCount());
    }

    @Test
    public void load_fileThatCannotBeRead_reportsReasonInsteadOfThrowing(@TempDir Path directory)
            throws IOException {
        // A directory sitting where the save file should be cannot be read as lines.
        Path file = directory.resolve("serangooner.txt");
        Files.createDirectory(file);

        Storage.LoadResult result = new Storage(file).load();

        assertEquals(List.of(), result.tasks());
        assertEquals(0, result.skippedLineCount());
        assertTrue(result.errorMessage().contains("couldn't read"));
    }

    @Test
    public void save_fileThatCannotBeWritten_exceptionThrown(@TempDir Path directory)
            throws IOException {
        Path file = directory.resolve("serangooner.txt");
        Files.createDirectory(file);

        Storage storage = new Storage(file);
        assertThrows(SerangoonerException.class, () -> storage.save(List.of(new Todo("read book"))));
    }

    @Test
    public void save_fileWithExistingTasks_replacesThemInsteadOfAppending(@TempDir Path directory)
            throws IOException {
        Path file = directory.resolve("serangooner.txt");
        Storage storage = new Storage(file);
        storage.save(List.of(new Todo("read book"), new Todo("water plants")));

        storage.save(List.of(new Todo("call mum")));

        List<String> lines = Files.readAllLines(file);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("call mum"));
    }

    @Test
    public void saveThenLoad_everyTaskTypeDone_restoresTheDoneState(@TempDir Path directory) {
        Storage storage = new Storage(directory.resolve("serangooner.txt"));
        Task todo = new Todo("read book");
        Task deadline = new Deadline("submit ip", "2026-09-01");
        Task event = new Event("orbital demo", "2026-09-05 1400", "2026-09-05 1600");
        todo.markDone();
        deadline.markDone();
        event.markDone();

        storage.save(List.of(todo, deadline, event));
        List<Task> loaded = storage.load().tasks();

        assertEquals(3, loaded.size());
        assertTrue(loaded.get(0).isDone());
        assertTrue(loaded.get(1).isDone());
        assertTrue(loaded.get(2).isDone());
    }

    @Test
    public void load_lineWithTooManyFields_skipsIt(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("serangooner.txt");
        Files.write(file, List.of("T | 0 | read book | and one field too many"));

        Storage.LoadResult result = new Storage(file).load();

        assertEquals(List.of(), result.tasks());
        assertEquals(1, result.skippedLineCount());
    }
}
