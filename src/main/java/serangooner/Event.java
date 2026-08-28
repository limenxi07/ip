package serangooner;

/**
 * Represents a task that starts and ends at specified dates or times.
 */
public class Event extends Task {
    private final String from;
    private final String to;

    /**
     * Constructs an event from values that have already been parsed.
     *
     * @param description Text describing what the event involves.
     * @param from Date or time at which the event starts.
     * @param to Date or time at which the event ends.
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Constructs an event by parsing a full user command.
     *
     * @param command Command in the form "event &lt;description&gt; from &lt;date&gt; to &lt;date&gt;".
     * @throws SerangoonerException If the command does not follow that form.
     */
    public Event(String command) {
        this(parse(command));
    }

    private Event(String[] parts) {
        this(parts[0], parts[1], parts[2]);
    }

    /**
     * Returns the description, start and end extracted from the given command.
     *
     * @param command Command in the form "event &lt;description&gt; from &lt;date&gt; to &lt;date&gt;".
     * @return Array holding the description, the start, then the end.
     * @throws SerangoonerException If any part is missing or blank.
     */
    private static String[] parse(String command) {
        // Parsing logic authored with Codex.
        int fromIndex = command.indexOf(" from ");
        int toIndex = command.indexOf(" to ", fromIndex);
        if (fromIndex <= 6 || toIndex <= fromIndex + 6
                || toIndex + 4 >= command.length()
                || command.substring(toIndex + 4).isBlank()) {
            throw new SerangoonerException(
                    "INVALID. pls use format: event <description> from <date> to <date>");
        }
        return new String[]{command.substring(6, fromIndex),
                command.substring(fromIndex + 6, toIndex), command.substring(toIndex + 4)};
    }

    @Override
    public String toString() {
        return "[E]" + (isDone() ? "[✓] " : "[ ] ")
                + getDescription() + " (from: " + from + " to: " + to + ")";
    }
}
