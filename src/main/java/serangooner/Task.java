package serangooner;

/**
 * Represents a single task item tracked by Serangooner.
 */
public class Task {
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

    @Override
    public String toString() {
        return (isDone ? "[✓] " : "[ ] ") + description;
    }
}
