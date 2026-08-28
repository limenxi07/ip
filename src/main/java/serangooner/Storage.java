package serangooner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Reads the task list from the hard disk and writes it back again.
 * The save file holds one task per line, in the format produced by
 * {@link Task#toSaveFormat()}. A line that cannot be understood is skipped
 * rather than aborting the whole load, so one damaged line never costs the
 * user the rest of the list.
 */
public class Storage {
    /** Number of fields in a saved todo, the smallest kind of line. */
    private static final int TODO_FIELDS = 3;
    private static final int DEADLINE_FIELDS = 4;
    private static final int EVENT_FIELDS = 5;
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
            try {
                tasks.add(parseTask(line));
            } catch (SerangoonerException exception) {
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
     *
     * @param line Single non-blank line read from the save file.
     * @return Task that the line describes.
     * @throws SerangoonerException If the line is not a task that can be understood.
     */
    private static Task parseTask(String line) {
        String[] fields = line.split(Pattern.quote(Task.SAVE_DELIMITER));
        if (fields.length < TODO_FIELDS) {
            throw corruptedLine();
        }

        Task task = switch (fields[0]) {
            case Todo.SAVE_CODE -> new Todo(readDescription(fields, TODO_FIELDS));
            case Deadline.SAVE_CODE -> new Deadline(readDescription(fields, DEADLINE_FIELDS),
                    readTrailingField(fields, 1));
            case Event.SAVE_CODE -> new Event(readDescription(fields, EVENT_FIELDS),
                    readTrailingField(fields, 2), readTrailingField(fields, 1));
            default -> throw corruptedLine();
        };

        if (fields[1].equals(Task.SAVE_DONE)) {
            task.markDone();
        } else if (!fields[1].equals(Task.SAVE_NOT_DONE)) {
            throw corruptedLine();
        }
        return task;
    }

    /**
     * Returns the description spanning the fields between the completion flag
     * and the trailing date fields.
     * Joining those fields back together keeps a description that itself
     * contains the delimiter intact.
     *
     * @param fields Fields the line was split into.
     * @param fieldCount Number of fields a line of this task type must have.
     * @return Description of the task.
     * @throws SerangoonerException If the line is too short or the description is blank.
     */
    private static String readDescription(String[] fields, int fieldCount) {
        if (fields.length < fieldCount) {
            throw corruptedLine();
        }
        String description = String.join(Task.SAVE_DELIMITER,
                Arrays.copyOfRange(fields, 2, fields.length - (fieldCount - TODO_FIELDS)));
        if (description.isBlank()) {
            throw corruptedLine();
        }
        return description;
    }

    /**
     * Returns one of the date fields at the end of the line.
     *
     * @param fields Fields the line was split into.
     * @param positionFromEnd Position of the wanted field, counting back from one at the end.
     * @return Contents of that field.
     * @throws SerangoonerException If the field is blank.
     */
    private static String readTrailingField(String[] fields, int positionFromEnd) {
        String field = fields[fields.length - positionFromEnd];
        if (field.isBlank()) {
            throw corruptedLine();
        }
        return field;
    }

    private static SerangoonerException corruptedLine() {
        return new SerangoonerException("corrupted line in the save file");
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
