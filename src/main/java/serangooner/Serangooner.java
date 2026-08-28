package serangooner;

/**
 * Runs the Serangooner chatbot as a command line program.
 */
public class Serangooner {
    /**
     * Greets the user, then runs each command entered until the user says bye.
     *
     * @param args Command line arguments, which are ignored.
     */
    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();

        TaskList tasks = new TaskList();
        ui.showLoadReport(tasks.getLoadReport());

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
            } catch (SerangoonerException exception) {
                ui.showError(exception.getMessage());
            }
            ui.showDivider();
        }
    }
}
