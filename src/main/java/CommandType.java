import java.util.Arrays;

/* represents a command that can be entered in serangooner; refactored by codex */
public enum CommandType {
    TODO("todo", "todo <description>", "add a task without a date or time"),
    DEADLINE("deadline", "deadline <description> by <date>", "add a task with a deadline"),
    EVENT("event", "event <description> from <date> to <date>", "add an event"),
    LIST("list", "list", "view all saved tasks"),
    MARK("mark", "mark <number>", "mark a task as done"),
    UNMARK("unmark", "unmark <number>", "mark a task as incomplete"),
    DELETE("delete", "delete <number>", "delete a task"),
    UNDO("undo", "undo", "undo the last task change"),
    HELP("help", "help", "show commands"),
    BYE("bye", "bye", "exit serangooner");

    private final String keyword;
    private final String syntax;
    private final String description;

    CommandType(String keyword, String syntax, String description) {
        this.keyword = keyword;
        this.syntax = syntax;
        this.description = description;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getDescription() {
        return description;
    }

    /* generate command library to help users */
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
        return output.toString();
    }

    /* search command library to find command matching first word of user input; by codex */
    public static CommandType fromInput(String input) {
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
