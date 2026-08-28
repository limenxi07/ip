package serangooner;

import java.util.Optional;
import java.util.function.Function;

/**
 * Represents a single task item tracked by Serangooner.
 */
public class Task {
    /** Separator written between the fields of a saved task. */
    public static final String SAVE_DELIMITER = " | ";
    /** Field value marking a saved task as completed. */
    public static final String SAVE_DONE = "1";
    /** Field value marking a saved task as not yet completed. */
    public static final String SAVE_NOT_DONE = "0";

    private final String description;
    private boolean isDone;

    /**
     * Constructs an incomplete task with the given description.
     *
     * @param description Text describing what the task involves.
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Marks this task as completed.
     */
    public void markDone() {
        this.isDone = true;
    }

    /**
     * Marks this task as not yet completed.
     */
    public void markNotDone() {
        this.isDone = false;
    }

    public boolean isDone() {
        return isDone;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns the task encoded by the given fields of a saved line.
     * A usable line has exactly the expected number of fields, none blank,
     * and a recognized completion flag.
     *
     * @param fields Fields that one line of the save file was split into.
     * @param fieldCount Number of fields a line of this task type must have.
     * @param factory Builds the task once the line is known to be usable.
     * @return Task the line describes, or nothing if it cannot be read.
     */
    protected static Optional<Task> readSaveLine(String[] fields, int fieldCount,
            Function<String[], Task> factory) {
        if (fields.length != fieldCount) {
            return Optional.empty();
        }
        for (String field : fields) {
            if (field.isBlank()) {
                return Optional.empty();
            }
        }
        boolean isDone = SAVE_DONE.equals(fields[1]);
        if (!isDone && !SAVE_NOT_DONE.equals(fields[1])) {
            return Optional.empty();
        }

        Task task = factory.apply(fields);
        if (isDone) {
            task.markDone();
        }
        return Optional.of(task);
    }

    /**
     * Returns this task encoded as one line of the save file.
     * Subclasses prepend their type code and append their own date fields.
     *
     * @return Completion flag and description, separated by the save delimiter.
     */
    public String toSaveFormat() {
        return (isDone ? SAVE_DONE : SAVE_NOT_DONE) + SAVE_DELIMITER + description;
    }

    @Override
    public String toString() {
        return (isDone ? "[✓] " : "[ ] ") + description;
    }
}
