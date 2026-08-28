package serangooner;

import java.time.LocalDate;
import java.util.Arrays;

import serangooner.command.AddCommand;
import serangooner.command.Command;
import serangooner.command.CommandType;
import serangooner.command.DeleteCommand;
import serangooner.command.ExitCommand;
import serangooner.command.HelpCommand;
import serangooner.command.ListCommand;
import serangooner.command.MarkCommand;
import serangooner.command.OnCommand;
import serangooner.command.UndoCommand;
import serangooner.command.UnmarkCommand;
import serangooner.task.DateRange;
import serangooner.task.Deadline;
import serangooner.task.Event;
import serangooner.task.TaskDateTime;
import serangooner.task.Todo;

// Command parsing gathered into one class with the help of Claude Code.
/**
 * Makes sense of the lines that the user types.
 * This class is the only one that knows how a command is written, so the
 * wording of a command can change here without any task or list having to
 * change with it. One line of input becomes one {@link Command}, already
 * holding the values the line carried: a task, a task number or a range of
 * dates, with nothing left for the command to work out later.
 * Surrounding space is the user's to leave in, so every method here trims
 * the line before making sense of it.
 * Reading the save file is a separate concern and is left to
 * {@link serangooner.storage.Storage Storage}.
 */
public class Parser {
    private static final String BY_SEPARATOR = " by ";
    private static final String FROM_SEPARATOR = " from ";
    private static final String TO_SEPARATOR = " to ";

    private Parser() {
        // A parser holds nothing of its own, so there is never a reason to build one.
    }

    /**
     * Returns the command that the given line of input asks for, ready to run.
     * Everything the command needs is worked out here, so that carrying it
     * out later cannot fail for want of understanding what was typed.
     *
     * @param fullCommand Line of input entered by the user.
     * @return Command the input asks for.
     * @throws SerangoonerException If the input names no command, or does not
     *         give that command what it needs.
     */
    public static Command parse(String fullCommand) {
        CommandType commandType = parseCommandType(fullCommand);
        return switch (commandType) {
            case TODO -> new AddCommand(parseTodo(fullCommand));
            case DEADLINE -> new AddCommand(parseDeadline(fullCommand));
            case EVENT -> new AddCommand(parseEvent(fullCommand));
            case MARK -> new MarkCommand(parseTaskNumber(fullCommand, commandType));
            case UNMARK -> new UnmarkCommand(parseTaskNumber(fullCommand, commandType));
            case DELETE -> new DeleteCommand(parseTaskNumber(fullCommand, commandType));
            case ON -> new OnCommand(parseDateRange(fullCommand));
            case LIST -> new ListCommand();
            case UNDO -> new UndoCommand();
            case HELP -> new HelpCommand();
            case BYE -> new ExitCommand();
        };
    }

    /**
     * Returns the command whose keyword matches the first word of the given input.
     *
     * @param input Line of input entered by the user.
     * @return Command matching the first word of the input.
     * @throws SerangoonerException If the input is blank or matches no command.
     */
    public static CommandType parseCommandType(String input) {
        // Lookup authored with Codex.
        if (input.isBlank()) {
            throw invalidCommand();
        }
        String firstWord = input.trim().split("\\s+", 2)[0];
        return Arrays.stream(CommandType.values())
                .filter(command -> command.getKeyword().equals(firstWord))
                .findFirst()
                .orElseThrow(Parser::invalidCommand);
    }

    /**
     * Returns the todo described by the given command.
     *
     * @param command Command in the form "todo &lt;description&gt;".
     * @return Todo the command describes.
     * @throws SerangoonerException If the description is missing.
     */
    public static Todo parseTodo(String command) {
        String description = argumentOf(command, CommandType.TODO);
        if (description.isEmpty()) {
            throw new SerangoonerException("pls name ur task: " + CommandType.TODO.getSyntax());
        }
        return new Todo(description);
    }

    /**
     * Returns the deadline described by the given command.
     *
     * @param rawCommand Command in the form "deadline &lt;description&gt; by &lt;date&gt;".
     * @return Deadline the command describes.
     * @throws SerangoonerException If the command does not follow that form, or
     *         its date is not in an accepted format.
     */
    public static Deadline parseDeadline(String rawCommand) {
        // Parsing logic authored with Codex.
        String command = rawCommand.trim();
        int descriptionStart = startOfArgument(CommandType.DEADLINE);
        int byIndex = command.indexOf(BY_SEPARATOR);
        int deadlineStart = byIndex + BY_SEPARATOR.length();
        if (byIndex <= descriptionStart || deadlineStart >= command.length()
                || command.substring(deadlineStart).isBlank()) {
            throw invalidFormat(CommandType.DEADLINE);
        }
        return new Deadline(command.substring(descriptionStart, byIndex),
                command.substring(deadlineStart));
    }

    /**
     * Returns the event described by the given command.
     *
     * @param rawCommand Command in the form "event &lt;description&gt; from &lt;date&gt; to &lt;date&gt;".
     * @return Event the command describes.
     * @throws SerangoonerException If the command does not follow that form, if
     *         either of its dates is not in an accepted format, or if the event
     *         ends before it starts.
     */
    public static Event parseEvent(String rawCommand) {
        // Parsing logic authored with Codex.
        String command = rawCommand.trim();
        int descriptionStart = startOfArgument(CommandType.EVENT);
        int fromIndex = command.indexOf(FROM_SEPARATOR);
        int toIndex = command.indexOf(TO_SEPARATOR, fromIndex);
        int fromStart = fromIndex + FROM_SEPARATOR.length();
        int toStart = toIndex + TO_SEPARATOR.length();
        if (fromIndex <= descriptionStart || toIndex <= fromStart
                || toStart >= command.length() || command.substring(toStart).isBlank()) {
            throw invalidFormat(CommandType.EVENT);
        }
        return new Event(command.substring(descriptionStart, fromIndex),
                command.substring(fromStart, toIndex), command.substring(toStart));
    }

    /**
     * Returns the task number given as the sole argument of the command.
     * Whether that number names a task is for the task list to say.
     *
     * @param command Command in the form "&lt;keyword&gt; &lt;number&gt;".
     * @param commandType Command that the input was recognized as.
     * @return Number the user typed.
     * @throws SerangoonerException If the number is missing or is not a number.
     */
    public static int parseTaskNumber(String command, CommandType commandType) {
        String argument = argumentOf(command, commandType);
        String label = commandType.name();
        if (argument.isEmpty()) {
            throw new SerangoonerException(label + " FAILED. pls use format: "
                    + commandType.getSyntax());
        }

        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException exception) {
            throw new SerangoonerException(label + " FAILED. pls give a valid task number", exception);
        }
    }

    /**
     * Returns the span of dates named by the given command.
     * A command naming a single date gives a range covering that day alone.
     *
     * @param command Command in the form "on &lt;date&gt;" or "on &lt;date&gt; to &lt;date&gt;".
     * @return Range of dates the command names.
     * @throws SerangoonerException If a date is missing, is not in an accepted
     *         format, or the range ends before it starts.
     */
    public static DateRange parseDateRange(String command) {
        String argument = argumentOf(command, CommandType.ON);
        if (argument.isEmpty()) {
            throw new SerangoonerException("pls give a date: " + CommandType.ON.getSyntax() + ". "
                    + TaskDateTime.FORMAT_HINT);
        }

        int toIndex = argument.indexOf(TO_SEPARATOR);
        if (toIndex < 0) {
            LocalDate date = TaskDateTime.parseDate(argument);
            return new DateRange(date, date);
        }
        return new DateRange(TaskDateTime.parseDate(argument.substring(0, toIndex)),
                TaskDateTime.parseDate(argument.substring(toIndex + TO_SEPARATOR.length())));
    }

    /**
     * Returns everything the user typed after the command keyword, trimmed.
     *
     * @param command Line of input entered by the user.
     * @param commandType Command that the input was recognized as.
     * @return Argument of the command, or an empty string if none was given.
     */
    private static String argumentOf(String command, CommandType commandType) {
        String trimmed = command.trim();
        int keywordLength = commandType.getKeyword().length();
        return trimmed.length() > keywordLength ? trimmed.substring(keywordLength).trim() : "";
    }

    /**
     * Returns the position at which the argument of the given command starts,
     * which is just past its keyword and the space that follows it.
     *
     * @param commandType Command that the input was recognized as.
     * @return Index of the first character after the keyword and its space.
     */
    private static int startOfArgument(CommandType commandType) {
        return commandType.getKeyword().length() + 1;
    }

    private static SerangoonerException invalidCommand() {
        return new SerangoonerException(
                "invalid command :/ if you don't know what you're doing, "
                        + "pls type 'help' for the command library .-.");
    }

    private static SerangoonerException invalidFormat(CommandType commandType) {
        return new SerangoonerException("INVALID. pls use format: " + commandType.getSyntax()
                + ". " + TaskDateTime.FORMAT_HINT);
    }
}
