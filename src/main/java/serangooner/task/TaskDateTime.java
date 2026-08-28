package serangooner.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import serangooner.SerangoonerException;

// Date handling authored with the help of Claude Code.
/**
 * Represents a point in time attached to a task, as a date with an optional
 * time of day.
 * A value is read from the format the user types, {@code 2019-10-15} or
 * {@code 2019-10-15 1800}, and shown back in a friendlier one,
 * {@code 15 Oct 2019} or {@code 15 Oct 2019, 6:00PM}.
 */
public class TaskDateTime {
    /** Wording of the accepted input formats, for use in error messages. */
    public static final String FORMAT_HINT = "dates must look like 2019-10-15 or 2019-10-15 1800";

    private static final DateTimeFormatter INPUT_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
    private static final DateTimeFormatter INPUT_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm", Locale.ENGLISH);
    private static final DateTimeFormatter OUTPUT_DATE =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter OUTPUT_DATE_TIME =
            DateTimeFormatter.ofPattern("dd MMM yyyy, h:mma", Locale.ENGLISH);

    private final LocalDateTime dateTime;
    private final boolean hasTime;

    private TaskDateTime(LocalDateTime dateTime, boolean hasTime) {
        this.dateTime = dateTime;
        this.hasTime = hasTime;
    }

    /**
     * Returns the point in time that the given text describes.
     * A bare date is held as the start of that day, and remembers that no
     * time of day was given so that none is shown back to the user.
     *
     * @param text Date on its own, or a date followed by a 24 hour time.
     * @return Point in time the text describes.
     * @throws SerangoonerException If the text is in neither accepted format.
     */
    public static TaskDateTime parse(String text) {
        String trimmed = text.trim();
        try {
            return new TaskDateTime(LocalDateTime.parse(trimmed, INPUT_DATE_TIME), true);
        } catch (DateTimeParseException exception) {
            // No time of day was given, so fall through and try a bare date.
        }

        try {
            return new TaskDateTime(LocalDate.parse(trimmed, INPUT_DATE).atStartOfDay(), false);
        } catch (DateTimeParseException exception) {
            throw new SerangoonerException("'" + trimmed + "' isn't a date i understand. "
                    + FORMAT_HINT, exception);
        }
    }

    /**
     * Returns the calendar date that the given text describes, dropping any
     * time of day it carries.
     *
     * @param text Date on its own, or a date followed by a 24 hour time.
     * @return Calendar date the text describes.
     * @throws SerangoonerException If the text is in neither accepted format.
     */
    public static LocalDate parseDate(String text) {
        return parse(text).toLocalDate();
    }

    /**
     * Returns the given date written the way it is shown to the user.
     *
     * @param date Date to write out.
     * @return Date in the form "15 Oct 2019".
     */
    public static String format(LocalDate date) {
        return date.format(OUTPUT_DATE);
    }

    /**
     * Returns whether this point in time comes before the given one.
     * A value carrying no time of day counts as the start of its day.
     *
     * @param other Point in time to compare against.
     * @return True if this value is the earlier of the two.
     */
    public boolean isBefore(TaskDateTime other) {
        return dateTime.isBefore(other.dateTime);
    }

    /**
     * Returns the calendar date this point in time falls on.
     */
    public LocalDate toLocalDate() {
        return dateTime.toLocalDate();
    }

    /**
     * Returns this point in time written the way the user types it, so that a
     * saved value reads back as the same value.
     *
     * @return Date, followed by a 24 hour time when one was given.
     */
    public String toSaveFormat() {
        return dateTime.format(hasTime ? INPUT_DATE_TIME : INPUT_DATE);
    }

    @Override
    public String toString() {
        return hasTime ? dateTime.format(OUTPUT_DATE_TIME) : format(toLocalDate());
    }
}
