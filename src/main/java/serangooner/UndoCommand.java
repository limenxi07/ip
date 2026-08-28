package serangooner;

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
