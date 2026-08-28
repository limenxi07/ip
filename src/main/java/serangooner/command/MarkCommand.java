package serangooner.command;

import serangooner.storage.Storage;
import serangooner.task.TaskList;
import serangooner.ui.Ui;

/**
 * Marks a task as done.
 */
public class MarkCommand extends Command {
    private final int taskNumber;

    /**
     * Constructs a command that marks the task at the given position.
     *
     * @param taskNumber Position of the task in the list, counting from one.
     */
    public MarkCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showTaskMarked(tasks.mark(taskNumber));
        storage.save(tasks.getTasks());
    }
}
