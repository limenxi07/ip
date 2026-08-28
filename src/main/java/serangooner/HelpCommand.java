package serangooner;

/**
 * Shows every command the user can enter.
 */
public class HelpCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showHelp(CommandType.values());
    }
}
