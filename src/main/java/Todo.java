/* regular task without date or time */
public class Todo extends Task {
    public Todo(String description) {
        super(description);
    }

    @Override
    public String toString() {
        return "[T]" + (isDone() ? "[✓] " : "[ ] ") + getDescription();
    }
}
