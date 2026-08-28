package serangooner;

import java.nio.file.Path;

/**
 * Runs the Serangooner chatbot as a command line program.
 * This class is the one place that holds the user interface, the task list
 * and the save file together, so it alone decides when a change to the list
 * is written back to disk.
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
                CommandType commandType = CommandType.fromInput(command);
                switch (commandType) {
                    case BYE -> {
                        ui.showFarewell();
                        return;
                    }
                    case HELP -> ui.showMessage(CommandType.helpText());
                    case LIST -> ui.showMessage(tasks.toString());
                    case ON -> ui.showMessage(tasks.occurringOn(command));
                    case UNDO -> ui.showMessage(tasks.undo()
                            ? "undid your last edit" : "there's nothing to undo >:(");
                    case MARK -> ui.showMessage(tasks.mark(command));
                    case UNMARK -> ui.showMessage(tasks.unmark(command));
                    case DELETE -> ui.showMessage(tasks.delete(command));
                    case TODO -> ui.showMessage(tasks.addTodo(command));
                    case DEADLINE -> ui.showMessage(tasks.addDeadline(command));
                    case EVENT -> ui.showMessage(tasks.addEvent(command));
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
     * Starts the chatbot on the default save file.
     *
     * @param args Command line arguments, which are ignored.
     */
    public static void main(String[] args) {
        new Serangooner(DEFAULT_FILE_PATH).run();
    }
}
