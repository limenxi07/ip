package serangooner;

/**
 * Represents a task that has no associated date or time.
 */
public class Todo extends Task {
    /**
     * Constructs a todo with the given description.
     *
     * @param description Text describing what the task involves.
     */
    public Todo(String description) {
        super(description);
    }

    @Override
    public String toString() {
        return "[T]" + (isDone() ? "[✓] " : "[ ] ") + getDescription();
    }
}
