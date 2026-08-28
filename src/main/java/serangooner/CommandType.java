package serangooner;

import java.util.Arrays;

// Enum-based command library refactored with the help of Codex.
/**
 * Represents a command that a user can enter in Serangooner.
 * Each constant pairs the keyword typed by the user with the syntax and
 * description shown in the help listing.
 */
public enum CommandType {
    TODO("todo", "todo <description>", "add a task without a date or time", true),
    DEADLINE("deadline", "deadline <description> by <date>", "add a task with a deadline", true),
    EVENT("event", "event <description> from <date> to <date>", "add an event", true),
    LIST("list", "list", "view all saved tasks", false),
    ON("on", "on <date> [to <date>]", "view deadlines and events on a date or within a range", false),
    MARK("mark", "mark <number>", "mark a task as done", true),
    UNMARK("unmark", "unmark <number>", "mark a task as incomplete", true),
    DELETE("delete", "delete <number>", "delete a task", true),
    UNDO("undo", "undo", "undo the last task change", true),
    HELP("help", "help", "show commands", false),
    BYE("bye", "bye", "exit serangooner", false);

    private final String keyword;
    private final String syntax;
    private final String description;
    private final boolean isMutating;

    CommandType(String keyword, String syntax, String description, boolean isMutating) {
        this.keyword = keyword;
        this.syntax = syntax;
        this.description = description;
        this.isMutating = isMutating;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns whether running this command can change the task list, and so
     * leaves the saved copy out of date until it is written again.
     */
    public boolean isMutating() {
        return isMutating;
    }

    /**
     * Returns a numbered listing of every command, its syntax and its description.
     *
     * @return Help text ready to be shown to the user.
     */
    public static String helpText() {
        StringBuilder output = new StringBuilder("serangooner commands:");
        int commandNumber = 1;
        for (CommandType command : values()) {
            output.append(System.lineSeparator())
                    .append(commandNumber++)
                    .append(". ")
                    .append(command.syntax)
                    .append(" - ")
                    .append(command.description);
        }
        return output.append(System.lineSeparator())
                .append(TaskDateTime.FORMAT_HINT)
                .toString();
    }

    /**
     * Returns the command whose keyword matches the first word of the given input.
     *
     * @param input Line of input entered by the user.
     * @return Command matching the first word of the input.
     * @throws SerangoonerException If the input is blank or matches no command.
     */
    public static CommandType fromInput(String input) {
        // Lookup authored with Codex.
        if (input.isBlank()) {
            throw invalidCommand();
        }
        String firstWord = input.trim().split("\\s+", 2)[0];
        return Arrays.stream(values())
                .filter(command -> command.keyword.equals(firstWord))
                .findFirst()
                .orElseThrow(CommandType::invalidCommand);
    }

    private static SerangoonerException invalidCommand() {
        return new SerangoonerException(
                "invalid command :/ if you don't know what you're doing, "
                        + "pls type 'help' for the command library .-.");
    }
}
