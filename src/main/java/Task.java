/* represents one task item stored by serangooner */
public class Task {
    private final String description;
    private boolean status; // completion status; false = incomplete

    public Task(String description) {
        this.description = description;
        this.status = false;
    }

    public void markDone() {
        this.status = true;
    }

    public void markNotDone() {
        this.status = false;
    }

    public boolean isDone() {
        return this.status;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() { // string representation with completion status
        return (status ? "[✓] " : "[ ] ") + description;
    }
}
