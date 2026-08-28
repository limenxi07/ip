package serangooner;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Represents a task that starts and ends at specified dates or times.
 */
public class Event extends Task {
    /** Code identifying an event in the save file. */
    public static final String SAVE_CODE = "E";
    private static final int SAVE_FIELDS = 5;

    private final TaskDateTime from;
    private final TaskDateTime to;

    /**
     * Constructs an event from a description and two unparsed dates.
     *
     * @param description Text describing what the event involves.
     * @param from Date, and optionally time, at which the event starts.
     * @param to Date, and optionally time, at which the event ends.
     * @throws SerangoonerException If either date is not in an accepted format.
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = TaskDateTime.parse(from);
        this.to = TaskDateTime.parse(to);
    }

    /**
     * Constructs an event by parsing a full user command.
     *
     * @param command Command in the form "event &lt;description&gt; from &lt;date&gt; to &lt;date&gt;".
     * @throws SerangoonerException If the command does not follow that form, or
     *         either of its dates is not in an accepted format.
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
                    "INVALID. pls use format: event <description> from <date> to <date>. "
                            + TaskDateTime.FORMAT_HINT);
        }
        return new String[]{command.substring(6, fromIndex),
                command.substring(fromIndex + 6, toIndex), command.substring(toIndex + 4)};
    }

    /**
     * Returns the event encoded by the given fields of a saved line.
     *
     * @param fields Fields that one line of the save file was split into.
     * @return Event the line describes, or nothing if it cannot be read.
     */
    static Optional<Task> fromSaveFields(String[] fields) {
        return readSaveLine(fields, SAVE_FIELDS, f -> new Event(f[2], f[3], f[4]));
    }

    @Override
    public boolean isWithin(LocalDate start, LocalDate end) {
        return isOverlapping(from, to, start, end);
    }

    @Override
    public String toSaveFormat() {
        return SAVE_CODE + SAVE_DELIMITER + super.toSaveFormat()
                + SAVE_DELIMITER + from.toSaveFormat() + SAVE_DELIMITER + to.toSaveFormat();
    }

    @Override
    public String toString() {
        return "[E]" + (isDone() ? "[✓] " : "[ ] ")
                + getDescription() + " (from: " + from + " to: " + to + ")";
    }
}
