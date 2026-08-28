package serangooner;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Deals with everything the user reads and types on the command line.
 * All of the wording that belongs to the program itself rather than to a
 * single command lives here, so that swapping the console for another kind
 * of interface only means replacing this class.
 */
public class Ui {
    private static final String DIVIDER = "━━━ . °‧ 𓆝 𓆟 𓆞 ·｡";
    private static final String BANNER =
            "  ____   U _____ u   ____        _      _   _     ____"
                    + "    U  ___ u   U  ___ u  _   _   U _____ u   ____     \n"
                    + " / __\"| u\\| ___\"|/U |  _\"\\ u U  /\"\\  u | \\ |\"| U /\"___"
                    + "|u   \\/\"_ \\/    \\/\"_ \\/ | \\ |\"|  \\| ___\"|/U |  _\"\\ u  \n"
                    + "<\\___ \\/  |  _|\"   \\| |_) |/  \\/ _ \\/ <|  \\| |>\\| |  _"
                    + " /   | | | |    | | | |<|  \\| |>  |  _|\"   \\| |_) |/  \n"
                    + " u___) |  | |___    |  _ <    / ___ \\ U| |\\  |u | |_| "
                    + "|.-,_| |_| |.-,_| |_| |U| |\\  |u  | |___    |  _ <    \n"
                    + " |____/>> |_____|   |_| \\_\\  /_/   \\_\\ |_| \\_|   \\____"
                    + "| \\_)-\\___/  \\_)-\\___/  |_| \\_|   |_____|   |_| \\_\\   \n"
                    + "  )(  (__)<<   >>   //   \\\\_  \\\\    >> ||   \\\\,-._)(|_"
                    + "       \\\\         \\\\    ||   \\\\,-.<<   >>   //   \\\\_  \n"
                    + " (__)    (__) (__) (__)  (__)(__)  (__)(_\")  (_/(__)__"
                    + ")     (__)       (__)   (_\")  (_/(__) (__) (__)  (__)  \n";

    private final Scanner scanner;
    private final PrintStream out;
    private final SoundPlayer soundPlayer;

    /**
     * Constructs a user interface that talks to the console.
     */
    public Ui() {
        this(System.in, System.out, true);
    }

    /**
     * Constructs a user interface that reads from and writes to the given
     * streams, without the sound that accompanies an error. Intended for
     * tests, which have no one listening and no console to read from.
     *
     * @param in Stream that commands are read from.
     * @param out Stream that messages are written to.
     */
    public Ui(InputStream in, PrintStream out) {
        this(in, out, false);
    }

    private Ui(InputStream in, PrintStream out, boolean isSoundEnabled) {
        this.scanner = new Scanner(in);
        this.out = out;
        this.soundPlayer = new SoundPlayer(isSoundEnabled);
    }

    /**
     * Greets the user and points out how to get help and how to leave.
     */
    public void showWelcome() {
        out.println(BANNER);
        out.println(DIVIDER);
        out.println("serangooner at your service. what's up?");
        out.println("to see commands, type 'help'");
        out.println("done? type 'bye' to exit :(");
        out.println(DIVIDER);
    }

    /**
     * Reports what was read from the save file, and says nothing at all when
     * there was neither a task nor a problem worth mentioning.
     *
     * @param report Outcome of reading the save file.
     */
    public void showLoadReport(Storage.LoadResult report) {
        String summary = describe(report);
        if (summary.isEmpty()) {
            return;
        }
        out.println(summary);
        out.println(DIVIDER);
    }

    /**
     * Returns the one line report to show for the given load outcome.
     *
     * @param report Outcome of reading the save file.
     * @return Report to show the user, or an empty string when there is
     *         nothing worth saying.
     */
    private static String describe(Storage.LoadResult report) {
        if (!report.errorMessage().isEmpty()) {
            return report.errorMessage();
        }
        if (report.tasks().isEmpty() && report.skippedLineCount() == 0) {
            return "";
        }
        String summary = "loaded " + report.tasks().size() + " task(s) from your last visit";
        if (report.skippedLineCount() > 0) {
            summary += "; skipped " + report.skippedLineCount() + " unreadable line(s)";
        }
        return summary;
    }

    /**
     * Returns whether the user has entered another command.
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Returns the next command entered by the user.
     */
    public String readCommand() {
        return scanner.nextLine();
    }

    /**
     * Shows every command the user can enter, with its syntax and what it does.
     *
     * @param commands Commands to list, in the order they should be shown.
     */
    public void showHelp(CommandType[] commands) {
        StringBuilder output = new StringBuilder("serangooner commands:");
        int commandNumber = 1;
        for (CommandType command : commands) {
            output.append(System.lineSeparator())
                    .append(commandNumber++)
                    .append(". ")
                    .append(command.getSyntax())
                    .append(" - ")
                    .append(command.getDescription());
        }
        out.println(output.append(System.lineSeparator()).append(TaskDateTime.FORMAT_HINT));
    }

    /**
     * Shows that the given task was added, and how many tasks now wait.
     *
     * @param task Task that was added.
     * @param taskCount Number of tasks the list now holds.
     */
    public void showTaskAdded(Task task, int taskCount) {
        out.println("added " + task.getTypeName() + ": " + task);
        out.println("you now have " + taskCount + " pending task(s) :c");
    }

    /**
     * Shows that the given task is now done.
     *
     * @param task Task that was marked.
     */
    public void showTaskMarked(Task task) {
        out.println("marked task as done: " + task);
    }

    /**
     * Shows that the given task is now incomplete.
     *
     * @param task Task that was unmarked.
     */
    public void showTaskUnmarked(Task task) {
        out.println("marked task as incomplete: " + task);
    }

    /**
     * Shows that the given task is gone from the list.
     *
     * @param task Task that was deleted.
     */
    public void showTaskDeleted(Task task) {
        out.println("deleted task: " + task);
    }

    /**
     * Shows whether the last edit was reversed.
     *
     * @param isUndone True if an edit was undone, false if there was none to undo.
     */
    public void showUndo(boolean isUndone) {
        out.println(isUndone ? "undid your last edit" : "there's nothing to undo >:(");
    }

    /**
     * Shows the whole task list.
     *
     * @param entries Every task, each with the number it is known by.
     */
    public void showTasks(List<TaskList.Entry> entries) {
        showListing(entries, "your list", "your list is empty T-T add something with 'todo ...'");
    }

    /**
     * Shows the tasks falling within a span of dates.
     *
     * @param entries Matching tasks, each with the number it is known by.
     * @param start First date of the span, inclusive.
     * @param end Last date of the span, inclusive.
     */
    public void showTasksInRange(List<TaskList.Entry> entries, LocalDate start, LocalDate end) {
        String range = start.equals(end)
                ? "on " + TaskDateTime.format(start)
                : "between " + TaskDateTime.format(start) + " and " + TaskDateTime.format(end);
        showListing(entries, "tasks " + range, "you have nothing " + range + " :D");
    }

    /**
     * Shows a numbered listing of the given tasks, or says so when there are none.
     *
     * @param entries Tasks to list, each with the number it is known by.
     * @param heading Line introducing the listing.
     * @param emptyMessage Message to show in place of an empty listing.
     */
    private void showListing(List<TaskList.Entry> entries, String heading, String emptyMessage) {
        if (entries.isEmpty()) {
            out.println(emptyMessage);
            return;
        }

        StringBuilder output = new StringBuilder(heading);
        for (TaskList.Entry entry : entries) {
            output.append(System.lineSeparator())
                    .append(" ")
                    .append(entry.number())
                    .append(". ")
                    .append(entry.task());
        }
        out.println(output);
    }

    /**
     * Shows the given error to the user, with a sound to match.
     *
     * @param message Explanation of what went wrong.
     */
    public void showError(String message) {
        soundPlayer.play();
        out.println(message);
    }

    /**
     * Draws the line that separates one exchange from the next.
     */
    public void showDivider() {
        out.println(DIVIDER);
    }

    /**
     * Says goodbye to the user.
     */
    public void showFarewell() {
        out.println("bye~");
        out.println(DIVIDER);
    }
}
