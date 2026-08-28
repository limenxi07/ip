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
     * @throws SerangoonerException If either date is not in an accepted format,
     *         or the event ends before it starts.
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = TaskDateTime.parse(from);
        this.to = TaskDateTime.parse(to);
        if (this.to.isBefore(this.from)) {
            throw new SerangoonerException("INVALID. ur event ends before it starts o.O");
        }
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
    public String getTypeName() {
        return "event";
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
