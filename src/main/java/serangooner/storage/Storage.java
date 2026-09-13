package serangooner.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import serangooner.SerangoonerException;
import serangooner.task.Deadline;
import serangooner.task.Event;
import serangooner.task.Task;
import serangooner.task.Todo;

/**
 * Reads the task list from the hard disk and writes it back again.
 * The save file holds one task per line, in the format produced by
 * {@link Task#toSaveFormat()} and read back by each task type's own
 * factory. A line that cannot be understood is skipped rather than
 * aborting the whole load, so one damaged line never costs the user the
 * rest of the list.
 */
public class Storage {
    private final Path filePath;

    /**
     * Constructs storage backed by the given file.
     *
     * @param filePath Relative path of the file that tasks are read from and written to.
     */
    public Storage(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Returns the tasks held in the save file.
     * A missing file is treated as an empty list, since that is simply the
     * first run of the program. A file that cannot be read is not fatal
     * either, so the reason is reported in the result rather than thrown.
     *
     * @return Tasks that were read, the number of lines skipped, and the
     *         reason the file could not be read.
     */
    public LoadResult load() {
        if (!Files.exists(filePath)) {
            return new LoadResult(new ArrayList<>(), 0, "");
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException exception) {
            return new LoadResult(new ArrayList<>(), 0,
                    "couldn't read " + filePath + ", so we're starting fresh");
        }

        List<Task> tasks = new ArrayList<>(lines.size());
        int skippedLineCount = 0;
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            Optional<Task> task = parseTask(line);
            if (task.isPresent()) {
                tasks.add(task.get());
            } else {
                skippedLineCount++;
            }
        }
        assert tasks.size() + skippedLineCount <= lines.size()
                : "every task read and every line skipped came from a line of the file";
        return new LoadResult(tasks, skippedLineCount, "");
    }

    /**
     * Writes the given tasks to the save file, replacing whatever it held before.
     * Any missing parent directory is created first.
     *
     * @param tasks Tasks to write, in the order they should be read back.
     * @throws SerangoonerException If the file cannot be written.
     */
    public void save(List<Task> tasks) {
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(filePath, tasks.stream().map(Task::toSaveFormat).toList());
        } catch (IOException exception) {
            throw new SerangoonerException("couldn't save your list to " + filePath
                    + " :( your change is only in memory", exception);
        }
    }

    /**
     * Returns the task encoded by the given line of the save file.
     * The type code decides which task type is asked to read the line, and
     * that type alone decides whether the remaining fields make sense.
     *
     * @param line Single non-blank line read from the save file.
     * @return Task the line describes, or nothing if it cannot be read.
     */
    private static Optional<Task> parseTask(String line) {
        assert !line.isBlank() : "load() skips blank lines, so a line here can name a task type";
        String[] fields = line.split(Pattern.quote(Task.SAVE_DELIMITER));
        return switch (fields[Task.SAVE_INDEX_TYPE]) {
            case Todo.SAVE_CODE -> Todo.parseSaveFields(fields);
            case Deadline.SAVE_CODE -> Deadline.parseSaveFields(fields);
            case Event.SAVE_CODE -> Event.parseSaveFields(fields);
            default -> Optional.empty();
        };
    }

    /**
     * Holds the outcome of reading the save file.
     *
     * @param tasks Tasks that were read successfully.
     * @param skippedLineCount Number of lines that could not be understood.
     * @param errorMessage Reason the file could not be read, or an empty
     *         string when it was read.
     */
    public record LoadResult(List<Task> tasks, int skippedLineCount, String errorMessage) {
    }
}
