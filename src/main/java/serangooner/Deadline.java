package serangooner;

/**
 * Represents a task that must be completed by a given date or time.
 */
public class Deadline extends Task {
    /** Code identifying a deadline in the save file. */
    public static final String SAVE_CODE = "D";

    private final String deadline;

    /**
     * Constructs a deadline from values that have already been parsed.
     *
     * @param description Text describing what the task involves.
     * @param deadline Date or time by which the task must be completed.
     */
    public Deadline(String description, String deadline) {
        super(description);
        this.deadline = deadline;
    }

    /**
     * Constructs a deadline by parsing a full user command.
     *
     * @param command Command in the form "deadline &lt;description&gt; by &lt;date&gt;".
     * @throws SerangoonerException If the command does not follow that form.
     */
    public Deadline(String command) {
        this(parse(command));
    }

    private Deadline(String[] parts) {
        this(parts[0], parts[1]);
    }

    /**
     * Returns the description and deadline extracted from the given command.
     *
     * @param command Command in the form "deadline &lt;description&gt; by &lt;date&gt;".
     * @return Array holding the description followed by the deadline.
     * @throws SerangoonerException If either part is missing or blank.
     */
    private static String[] parse(String command) {
        // Parsing logic authored with Codex.
        int byIndex = command.indexOf(" by ");
        if (byIndex <= 9 || byIndex + 4 >= command.length()
                || command.substring(byIndex + 4).isBlank()) {
            throw new SerangoonerException("INVALID. pls use format: deadline <description> by <date>");
        }
        return new String[]{command.substring(9, byIndex), command.substring(byIndex + 4)};
    }

    @Override
    public String toSaveFormat() {
        return SAVE_CODE + SAVE_DELIMITER + super.toSaveFormat() + SAVE_DELIMITER + deadline;
    }

    @Override
    public String toString() {
        return "[D]" + (isDone() ? "[✓] " : "[ ] ")
                + getDescription() + " (by: " + deadline + ")";
    }
}
