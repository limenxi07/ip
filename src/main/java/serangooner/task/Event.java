package serangooner.task;

import java.time.LocalDate;
import java.util.Optional;

import serangooner.SerangoonerException;

/**
 * Represents a task that starts and ends at specified dates or times.
 */
public class Event extends Task {
    /** Code identifying an event in the save file. */
    public static final String SAVE_CODE = "E";
    private static final int SAVE_FIELD_COUNT = 5;

    private final TaskDateTime startDateTime;
    private final TaskDateTime endDateTime;

    /**
     * Constructs an event from a description and two unparsed dates.
     *
     * @param description Text describing what the event involves.
     * @param startDateTime Date, and optionally time, at which the event starts.
     * @param endDateTime Date, and optionally time, at which the event ends.
     * @throws SerangoonerException If either date is not in an accepted format,
     *         or the event ends before it starts.
     */
    public Event(String description, String startDateTime, String endDateTime) {
        super(description);
        this.startDateTime = TaskDateTime.parse(startDateTime);
        this.endDateTime = TaskDateTime.parse(endDateTime);
        if (this.endDateTime.isBefore(this.startDateTime)) {
            throw new SerangoonerException("INVALID. ur event ends before it starts o.O");
        }
    }

    /**
     * Returns the event encoded by the given fields of a saved line.
     *
     * @param fields Fields that one line of the save file was split into.
     * @return Event the line describes, or nothing if it cannot be read.
     */
    public static Optional<Task> parseSaveFields(String[] fields) {
        return buildFromSaveFields(fields, SAVE_FIELD_COUNT,
                validFields -> new Event(validFields[2], validFields[3], validFields[4]));
    }

    @Override
    public String getTypeName() {
        return "event";
    }

    @Override
    public boolean isWithin(LocalDate start, LocalDate end) {
        return isOverlapping(startDateTime, endDateTime, start, end);
    }

    @Override
    public String toSaveFormat() {
        return SAVE_CODE + SAVE_DELIMITER + super.toSaveFormat()
                + SAVE_DELIMITER + startDateTime.toSaveFormat()
                + SAVE_DELIMITER + endDateTime.toSaveFormat();
    }

    @Override
    public String toString() {
        return "[E]" + super.toString()
                + " (from: " + startDateTime + " to: " + endDateTime + ")";
    }
}
