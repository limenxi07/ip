package serangooner.command;

import serangooner.storage.Storage;
import serangooner.task.TaskList;
import serangooner.ui.Ui;

/**
 * Says goodbye and brings the program to a stop.
 */
public class ExitCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showFarewell();
    }

    @Override
    public boolean isExit() {
        return true;
    }
}
