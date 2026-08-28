package serangooner;

/**
 * Marks a task as incomplete.
 */
public class UnmarkCommand extends Command {
    private final int taskNumber;

    /**
     * Constructs a command that unmarks the task at the given position.
     *
     * @param taskNumber Position of the task in the list, counting from one.
     */
    public UnmarkCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showTaskUnmarked(tasks.unmark(taskNumber));
        storage.save(tasks.getTasks());
    }
}
