/** task that starts and events at a specified time */
public class Event extends Task {
    private final String from;
    private final String to;

    public Event(String description, String from, String to) { // already parsed
        super(description);
        this.from = from;
        this.to = to;
    }

    public Event(String command) { // auto parsed
        this(parse(command));
    }

    private Event(String[] parts) {
        this(parts[0], parts[1], parts[2]);
    }

    private static String[] parse(String command) { // parsing logic by codex
        int fromIndex = command.indexOf(" /from ");
        int toIndex = command.indexOf(" /to ", fromIndex);
        if (fromIndex <= 6 || toIndex <= fromIndex + 7
                || toIndex + 5 >= command.length()
                || command.substring(toIndex + 5).isBlank()) {
            throw new IllegalArgumentException(
                    "INVALID. pls use format: event <description> /from <date/time> /to <date/time>");
        }
        return new String[]{command.substring(6, fromIndex),
                command.substring(fromIndex + 7, toIndex), command.substring(toIndex + 5)};
    }

    @Override
    public String toString() {
        return "[E]" + (isDone() ? "[✓] " : "[ ] ")
                + getDescription() + " (from: " + from + " to: " + to + ")";
    }
}
