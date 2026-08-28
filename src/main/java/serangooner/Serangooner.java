package serangooner;

import java.nio.file.Path;

/**
 * Runs the Serangooner chatbot as a command line program.
 * This class is the one place that holds the user interface, the task list
 * and the save file together, so it alone decides when a change to the list
 * is written back to disk. A command read from the user is handed to the
 * {@link Parser}, which says what was meant, and the result of acting on it
 * is handed back to the {@link Ui}.
 */
public class Serangooner {
    private static final String DEFAULT_FILE_PATH = "data/serangooner.txt";

    private final Ui ui;
    private final Storage storage;
    private final TaskList tasks;
    private final Storage.LoadResult loadResult;

    /**
     * Constructs a chatbot whose tasks are read from and written to the given file.
     * A file that cannot be read is not fatal: the chatbot starts with an
     * empty list and says why once the user has been greeted.
     *
     * @param filePath Relative path of the save file.
     */
    public Serangooner(String filePath) {
        this.ui = new Ui();
        this.storage = new Storage(Path.of(filePath));
        this.loadResult = storage.load();
        this.tasks = new TaskList(loadResult.tasks());
    }

    /**
     * Greets the user, then runs each command entered until the user says bye.
     * A command that can change the list is followed by a write to the save
     * file, so what is on disk keeps matching what the user sees.
     */
    public void run() {
        ui.showWelcome();
        ui.showLoadReport(loadResult);

        while (ui.hasNextCommand()) {
            String command = ui.readCommand();
            try {
                CommandType commandType = Parser.parseCommandType(command);
                switch (commandType) {
                    case BYE -> {
                        ui.showFarewell();
                        return;
                    }
                    case HELP -> ui.showHelp(CommandType.values());
                    case LIST -> ui.showTasks(tasks.entries());
                    case ON -> showOccurringOn(Parser.parseDateRange(command));
                    case UNDO -> ui.showUndo(tasks.undo());
                    case MARK -> ui.showTaskMarked(tasks.mark(Parser.parseTaskNumber(command, commandType)));
                    case UNMARK -> ui.showTaskUnmarked(
                            tasks.unmark(Parser.parseTaskNumber(command, commandType)));
                    case DELETE -> ui.showTaskDeleted(
                            tasks.delete(Parser.parseTaskNumber(command, commandType)));
                    case TODO -> showAdded(tasks.add(Parser.parseTodo(command)));
                    case DEADLINE -> showAdded(tasks.add(Parser.parseDeadline(command)));
                    case EVENT -> showAdded(tasks.add(Parser.parseEvent(command)));
                }
                if (commandType.isMutating()) {
                    storage.save(tasks.getTasks());
                }
            } catch (SerangoonerException exception) {
                ui.showError(exception.getMessage());
            }
            ui.showDivider();
        }
    }

    /**
     * Shows that the given task was added, along with the size the list has
     * reached now that it is in.
     *
     * @param task Task that was just added.
     */
    private void showAdded(Task task) {
        ui.showTaskAdded(task, tasks.size());
    }

    /**
     * Shows the tasks falling within the given span of dates.
     *
     * @param range Span of dates to report on.
     */
    private void showOccurringOn(Parser.DateRange range) {
        ui.showTasksInRange(tasks.occurringOn(range.start(), range.end()),
                range.start(), range.end());
    }

    /**
     * Starts the chatbot on the default save file.
     *
     * @param args Command line arguments, which are ignored.
     */
    public static void main(String[] args) {
        new Serangooner(DEFAULT_FILE_PATH).run();
    }
}
