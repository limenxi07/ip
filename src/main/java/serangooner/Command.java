package serangooner;

/**
 * Represents one thing the user has asked Serangooner to do.
 * A command is built by the {@link Parser} once, already knowing everything
 * it needs from the line that was typed, and is then run against the task
 * list. Running it is the only step left, so the main loop never has to ask
 * what kind of command it is holding.
 */
public abstract class Command {
    /**
     * Carries out this command.
     * A command that changes the list is responsible for writing the list
     * back to storage, so that what is on disk keeps matching what the user
     * sees.
     *
     * @param tasks Task list to act on.
     * @param ui User interface to report the outcome through.
     * @param storage Save file to write any change to.
     * @throws SerangoonerException If the command cannot be carried out.
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage);

    /**
     * Returns whether the program should stop after this command.
     */
    public boolean isExit() {
        return false;
    }
}
