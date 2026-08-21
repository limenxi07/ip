/* task to be completed by a given date/time */
public class Deadline extends Task {
    private final String deadline;

    public Deadline(String description, String deadline) { // already parsed
        super(description);
        this.deadline = deadline;
    }

    public Deadline(String command) { // auto parsed
        this(parse(command));
    }

    private Deadline(String[] parts) {
        this(parts[0], parts[1]);
    }

    private static String[] parse(String command) { // parsing logic by codex
        int byIndex = command.indexOf(" by ");
        if (byIndex <= 9 || byIndex + 4 >= command.length()
                || command.substring(byIndex + 4).isBlank()) {
            throw new SerangoonerException("INVALID. pls use format: deadline <description> by <date>");
        }
        return new String[]{command.substring(9, byIndex), command.substring(byIndex + 4)};
    }

    @Override
    public String toString() {
        return "[D]" + (isDone() ? "[✓] " : "[ ] ")
                + getDescription() + " (by: " + deadline + ")";
    }
}
