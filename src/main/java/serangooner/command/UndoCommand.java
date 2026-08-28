package serangooner.command;

import serangooner.storage.Storage;
import serangooner.task.TaskList;
import serangooner.ui.Ui;

/**
 * Reverses the most recent change made to the list.
 */
public class UndoCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showUndo(tasks.undo());
        storage.save(tasks.getTasks());
    }
}
