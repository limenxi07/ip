package serangooner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Reads the task list from the hard disk and writes it back again.
 * The save file holds one task per line, in the format produced by
 * {@link Task#toSaveFormat()} and read back by each task type's own
 * factory. A line that cannot be understood is skipped rather than
 * aborting the whole load, so one damaged line never costs the user the
 * rest of the list.
 */
public class Storage {
    private static final Path DEFAULT_FILE = Path.of("data", "serangooner.txt");

    private final Path file;

    /**
     * Constructs storage backed by the default save file, ./data/serangooner.txt.
     */
    public Storage() {
        this(DEFAULT_FILE);
    }

    /**
     * Constructs storage backed by the given file.
     *
     * @param file Relative path of the file that tasks are read from and written to.
     */
    public Storage(Path file) {
        this.file = file;
    }

    /**
     * Returns the tasks held in the save file.
     * A missing file is treated as an empty list, since that is simply the
     * first run of the program.
     *
     * @return Tasks that were read, together with the number of lines skipped.
     * @throws SerangoonerException If the file exists but cannot be read.
     */
    public LoadResult load() {
        if (!Files.exists(file)) {
            return new LoadResult(new ArrayList<>(), 0);
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException exception) {
            throw new SerangoonerException("couldn't read " + file
                    + ", so we're starting fresh", exception);
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
        return new LoadResult(tasks, skippedLineCount);
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
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, tasks.stream().map(Task::toSaveFormat).toList());
        } catch (IOException exception) {
            throw new SerangoonerException("couldn't save your list to " + file
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
        String[] fields = line.split(Pattern.quote(Task.SAVE_DELIMITER));
        return switch (fields[0]) {
            case Todo.SAVE_CODE -> Todo.fromSaveFields(fields);
            case Deadline.SAVE_CODE -> Deadline.fromSaveFields(fields);
            case Event.SAVE_CODE -> Event.fromSaveFields(fields);
            default -> Optional.empty();
        };
    }

    /**
     * Holds the outcome of reading the save file.
     *
     * @param tasks Tasks that were read successfully.
     * @param skippedLineCount Number of lines that could not be understood.
     */
    public record LoadResult(List<Task> tasks, int skippedLineCount) {
    }
}
