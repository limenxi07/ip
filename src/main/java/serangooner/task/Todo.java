package serangooner.task;

import java.util.Optional;

/**
 * Represents a task that has no associated date or time.
 */
public class Todo extends Task {
    /** Code identifying a todo in the save file. */
    public static final String SAVE_CODE = "T";
    private static final int SAVE_FIELDS = 3;

    /**
     * Constructs a todo with the given description.
     *
     * @param description Text describing what the task involves.
     */
    public Todo(String description) {
        super(description);
    }

    /**
     * Returns the todo encoded by the given fields of a saved line.
     *
     * @param fields Fields that one line of the save file was split into.
     * @return Todo the line describes, or nothing if it cannot be read.
     */
    public static Optional<Task> fromSaveFields(String[] fields) {
        return readSaveLine(fields, SAVE_FIELDS, f -> new Todo(f[2]));
    }

    @Override
    public String getTypeName() {
        return "todo";
    }

    @Override
    public String toSaveFormat() {
        return SAVE_CODE + SAVE_DELIMITER + super.toSaveFormat();
    }

    @Override
    public String toString() {
        return "[T]" + (isDone() ? "[✓] " : "[ ] ") + getDescription();
    }
}
