package serangooner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Runs the Serangooner chatbot as a command line program.
 */
public class Serangooner {
    private static final Path FAAAH = Path.of("src/main/resources/faaah.mp3");
    private static final String DIVIDER = "━━━ . °‧ 𓆝 𓆟 𓆞 ·｡";
    private static final String BANNER =
            "  ____   U _____ u   ____        _      _   _     ____"
                    + "    U  ___ u   U  ___ u  _   _   U _____ u   ____     \n"
                    + " / __\"| u\\| ___\"|/U |  _\"\\ u U  /\"\\  u | \\ |\"| U /\"___"
                    + "|u   \\/\"_ \\/    \\/\"_ \\/ | \\ |\"|  \\| ___\"|/U |  _\"\\ u  \n"
                    + "<\\___ \\/  |  _|\"   \\| |_) |/  \\/ _ \\/ <|  \\| |>\\| |  _"
                    + " /   | | | |    | | | |<|  \\| |>  |  _|\"   \\| |_) |/  \n"
                    + " u___) |  | |___    |  _ <    / ___ \\ U| |\\  |u | |_| "
                    + "|.-,_| |_| |.-,_| |_| |U| |\\  |u  | |___    |  _ <    \n"
                    + " |____/>> |_____|   |_| \\_\\  /_/   \\_\\ |_| \\_|   \\____"
                    + "| \\_)-\\___/  \\_)-\\___/  |_| \\_|   |_____|   |_| \\_\\   \n"
                    + "  )(  (__)<<   >>   //   \\\\_  \\\\    >> ||   \\\\,-._)(|_"
                    + "       \\\\         \\\\    ||   \\\\,-.<<   >>   //   \\\\_  \n"
                    + " (__)    (__) (__) (__)  (__)(__)  (__)(_\")  (_/(__)__"
                    + ")     (__)       (__)   (_\")  (_/(__) (__) (__)  (__)  \n";
    /**
     * Greets the user, then runs each command entered until the user says bye.
     *
     * @param args Command line arguments, which are ignored.
     */
    public static void main(String[] args) {
        System.out.println(BANNER);
        System.out.println(DIVIDER);
        System.out.println("serangooner at your service. what's up?");
        System.out.println("to see commands, type 'help'");
        System.out.println("done? type 'bye' to exit :(");
        System.out.println(DIVIDER);

        TaskList tasks = new TaskList();
        String loadSummary = tasks.getLoadSummary();
        if (!loadSummary.isEmpty()) {
            System.out.println(loadSummary);
            System.out.println(DIVIDER);
        }

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            try {
                CommandType commandType = CommandType.fromInput(command);
                switch (commandType) {
                    case BYE -> {
                        System.out.println("bye~");
                        System.out.println(DIVIDER);
                        return;
                    }
                    case HELP -> System.out.println(CommandType.helpText());
                    case LIST -> System.out.println(tasks);
                    case UNDO -> System.out.println(tasks.undo()
                            ? "undid your last edit" : "there's nothing to undo >:(");
                    case MARK -> System.out.println(tasks.mark(command));
                    case UNMARK -> System.out.println(tasks.unmark(command));
                    case DELETE -> System.out.println(tasks.delete(command));
                    case TODO -> System.out.println(tasks.addTodo(command));
                    case DEADLINE -> System.out.println(tasks.addDeadline(command));
                    case EVENT -> System.out.println(tasks.addEvent(command));
                }
            } catch (SerangoonerException exception) {
                playSound();
                System.out.println(exception.getMessage());
            }
            System.out.println(DIVIDER);
        }
    }

    /**
     * Plays a funny sound to accompany an invalid command.
     * The sound plays on a separate thread so that it never delays the next
     * prompt, and is skipped when the audio file is absent.
     */
    private static void playSound() {
        // Authored with the help of Codex.
        if (!Files.exists(FAAAH)) {
            return;
        }

        Thread.ofVirtual().start(() -> {
            try {
                new ProcessBuilder("afplay", FAAAH.toString()).start().waitFor();
            } catch (IOException | InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
