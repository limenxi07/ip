package serangooner.ui;

import java.time.LocalDate;
import java.util.List;

import serangooner.storage.Storage;
import serangooner.task.Task;
import serangooner.task.TaskDateTime;
import serangooner.task.TaskList;

/**
 * Composes everything the chatbot says to the user.
 * All of the wording that belongs to the program itself rather than to a
 * single command lives here. Every method hands its message back instead of
 * printing it, so that the console and the chat window can show the same
 * words while each decides for itself how they are presented.
 */
public class Ui {
    /**
     * Returns the greeting that opens a conversation.
     */
    public String formatWelcome() {
        return joinLines("serangooner at your service. what's up?",
                "to see commands, type 'help'",
                "done? type 'bye' to exit :(");
    }

    /**
     * Returns what to say about the save file that was just read, and an empty
     * string when there was neither a task nor a problem worth mentioning.
     *
     * @param report Outcome of reading the save file.
     */
    public String formatLoadReport(Storage.LoadResult report) {
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
     * Returns the given commands as a numbered list, ending with the note on
     * how a date must be written.
     * What each line says is for the caller to decide; this class only decides
     * how the listing is laid out.
     *
     * @param commands Descriptions to list, in the order they should be shown.
     */
    public String formatHelp(List<String> commands) {
        StringBuilder output = new StringBuilder("serangooner commands:");
        int commandNumber = 1;
        for (String command : commands) {
            output.append(System.lineSeparator())
                    .append(commandNumber++)
                    .append(". ")
                    .append(command);
        }
        return output.append(System.lineSeparator()).append(TaskDateTime.FORMAT_HINT).toString();
    }

    /**
     * Returns word that the given task was added, and how many tasks now wait.
     *
     * @param task Task that was added.
     * @param taskCount Number of tasks the list now holds.
     */
    public String formatTaskAdded(Task task, int taskCount) {
        assert taskCount > 0 : "a list that has just gained a task cannot be empty";
        return joinLines("added " + task.getTypeName() + ": " + task,
                "you now have " + taskCount + " pending task(s) :c");
    }

    /**
     * Returns word that the given task is now done.
     *
     * @param task Task that was marked.
     */
    public String formatTaskMarked(Task task) {
        return "marked task as done: " + task;
    }

    /**
     * Returns word that the given task is now incomplete.
     *
     * @param task Task that was unmarked.
     */
    public String formatTaskUnmarked(Task task) {
        return "marked task as incomplete: " + task;
    }

    /**
     * Returns word that the given task is gone from the list.
     *
     * @param task Task that was deleted.
     */
    public String formatTaskDeleted(Task task) {
        return "deleted task: " + task;
    }

    /**
     * Returns word of whether the last edit was reversed.
     *
     * @param isUndone True if an edit was undone, false if there was none to undo.
     */
    public String formatUndo(boolean isUndone) {
        return isUndone ? "undid your last edit" : "there's nothing to undo >:(";
    }

    /**
     * Returns the whole task list.
     *
     * @param entries Every task, each with the number it is known by.
     */
    public String formatTasks(List<TaskList.Entry> entries) {
        return formatListing(entries, "your list",
                "your list is empty T-T add something with 'todo ...'");
    }

    /**
     * Returns the tasks falling within a span of dates.
     *
     * @param entries Matching tasks, each with the number it is known by.
     * @param start First date of the span, inclusive.
     * @param end Last date of the span, inclusive.
     */
    public String formatTasksInRange(List<TaskList.Entry> entries, LocalDate start, LocalDate end) {
        String range = start.equals(end)
                ? "on " + TaskDateTime.format(start)
                : "between " + TaskDateTime.format(start) + " and " + TaskDateTime.format(end);
        return formatListing(entries, "tasks " + range, "you have nothing " + range + " :D");
    }

    /**
     * Returns the tasks whose description carries a keyword.
     *
     * @param entries Matching tasks, each with the number it is known by.
     * @param keyword Text that was searched for.
     */
    public String formatMatchingTasks(List<TaskList.Entry> entries, String keyword) {
        return formatListing(entries, "matching tasks:", "no task mentions '" + keyword + "' :o");
    }

    /**
     * Returns the farewell that closes a conversation.
     */
    public String formatFarewell() {
        return "bye~";
    }

    /**
     * Returns a numbered listing of the given tasks, or the stand-in message
     * when there are none.
     *
     * @param entries Tasks to list, each with the number it is known by.
     * @param heading Line introducing the listing.
     * @param emptyMessage Message to return in place of an empty listing.
     */
    private static String formatListing(List<TaskList.Entry> entries, String heading,
            String emptyMessage) {
        if (entries.isEmpty()) {
            return emptyMessage;
        }

        StringBuilder output = new StringBuilder(heading);
        for (TaskList.Entry entry : entries) {
            output.append(System.lineSeparator())
                    .append(" ")
                    .append(entry.number())
                    .append(". ")
                    .append(entry.task());
        }
        return output.toString();
    }

    /**
     * Returns the given lines as one message, one line below the other.
     *
     * @param lines Lines to join, in the order they should be shown.
     */
    private static String joinLines(String... lines) {
        return String.join(System.lineSeparator(), lines);
    }
}
