package serangooner.task;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Function;

import serangooner.SerangoonerException;

/**
 * Represents a single task item tracked by Serangooner.
 * A task on its own is not a complete thing to store: it is a task of some
 * kind, and its kind decides how it is named, whether it falls on a date and
 * what code identifies it in the save file. Only those kinds can be built.
 */
public abstract class Task {
    /** Separator written between the fields of a saved task. */
    public static final String SAVE_DELIMITER = " | ";
    /** Field value marking a saved task as completed. */
    public static final String SAVE_DONE = "1";
    /** Field value marking a saved task as not yet completed. */
    public static final String SAVE_NOT_DONE = "0";

    private final String description;
    private boolean isDone;

    /**
     * Constructs an incomplete task with the given description.
     *
     * @param description Text describing what the task involves.
     */
    protected Task(String description) {
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

    /**
     * Returns the name this kind of task goes by in messages to the user.
     */
    public abstract String getTypeName();

    /**
     * Returns whether this task falls on or between the given dates.
     * A task that carries no date of its own never does.
     *
     * @param start First date of the range, inclusive.
     * @param end Last date of the range, inclusive.
     * @return True if this task falls within the range.
     */
    public boolean isWithin(LocalDate start, LocalDate end) {
        return false;
    }

    /**
     * Returns whether the span a task occupies overlaps the given range of dates.
     * A task that sits at a single point in time passes that point as both
     * ends of its span.
     *
     * @param startDateTime Start of the span the task occupies.
     * @param endDateTime End of the span the task occupies.
     * @param start First date of the range, inclusive.
     * @param end Last date of the range, inclusive.
     * @return True if the span and the range share at least one date.
     */
    protected static boolean isOverlapping(TaskDateTime startDateTime, TaskDateTime endDateTime,
            LocalDate start, LocalDate end) {
        return !startDateTime.toLocalDate().isAfter(end)
                && !endDateTime.toLocalDate().isBefore(start);
    }

    /**
     * Returns the task encoded by the given fields of a saved line.
     * A usable line has exactly the expected number of fields, none blank,
     * a recognized completion flag, and remaining fields that the task type
     * itself accepts.
     *
     * @param fields Fields that one line of the save file was split into.
     * @param fieldCount Number of fields a line of this task type must have.
     * @param factory Builds the task once the line is known to be usable.
     * @return Task the line describes, or nothing if it cannot be read.
     */
    protected static Optional<Task> buildFromSaveFields(String[] fields, int fieldCount,
            Function<String[], Task> factory) {
        if (fields.length != fieldCount) {
            return Optional.empty();
        }
        for (String field : fields) {
            if (field.isBlank()) {
                return Optional.empty();
            }
        }
        boolean isDone = SAVE_DONE.equals(fields[1]);
        if (!isDone && !SAVE_NOT_DONE.equals(fields[1])) {
            return Optional.empty();
        }

        final Task task;
        try {
            task = factory.apply(fields);
        } catch (SerangoonerException exception) {
            return Optional.empty();
        }
        if (isDone) {
            task.markDone();
        }
        return Optional.of(task);
    }

    /**
     * Returns this task encoded as one line of the save file.
     * Subclasses prepend their type code and append their own date fields.
     *
     * @return Completion flag and description, separated by the save delimiter.
     */
    public String toSaveFormat() {
        return (isDone ? SAVE_DONE : SAVE_NOT_DONE) + SAVE_DELIMITER + description;
    }

    @Override
    public String toString() {
        return (isDone ? "[✓] " : "[ ] ") + description;
    }
}
