package serangooner;

import java.nio.file.Path;

/**
 * Runs the Serangooner chatbot as a command line program.
 * This class is the one place that holds the user interface, the task list
 * and the save file together, so it alone decides when a change to the list
 * is written back to disk. A line read from the user is handed to the
 * {@link Parser}, which returns the {@link Command} it asks for, and running
 * that command is all this class has left to do.
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
     * Every line is turned into a command that knows how to carry itself out,
     * so this loop never has to ask what was typed: it reads, runs, and draws
     * a divider, whether the command succeeded or not.
     */
    public void run() {
        ui.showWelcome();
        ui.showLoadReport(loadResult);

        boolean isExit = false;
        while (!isExit && ui.hasNextCommand()) {
            try {
                String fullCommand = ui.readCommand();
                Command command = Parser.parse(fullCommand);
                command.execute(tasks, ui, storage);
                isExit = command.isExit();
            } catch (SerangoonerException exception) {
                ui.showError(exception.getMessage());
            } finally {
                ui.showDivider();
            }
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
