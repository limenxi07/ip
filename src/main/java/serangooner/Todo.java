package serangooner;

/**
 * Represents a task that has no associated date or time.
 */
public class Todo extends Task {
    /** Code identifying a todo in the save file. */
    public static final String SAVE_CODE = "T";

    /**
     * Constructs a todo with the given description.
     *
     * @param description Text describing what the task involves.
     */
    public Todo(String description) {
        super(description);
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
