package serangooner;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Deals with everything the user reads and types on the command line.
 * All of the wording that belongs to the program itself rather than to a
 * single command lives here, so that swapping the console for another kind
 * of interface only means replacing this class.
 */
public class Ui {
    private static final Path SOUND_FILE = Path.of("src/main/resources/faaah.mp3");
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

    private final Scanner scanner;
    private final PrintStream out;
    private final boolean isSoundEnabled;

    /**
     * Constructs a user interface that talks to the console.
     */
    public Ui() {
        this(System.in, System.out, true);
    }

    /**
     * Constructs a user interface that reads from and writes to the given
     * streams, without the sound that accompanies an error. Intended for
     * tests, which have no one listening and no console to read from.
     *
     * @param in Stream that commands are read from.
     * @param out Stream that messages are written to.
     */
    public Ui(InputStream in, PrintStream out) {
        this(in, out, false);
    }

    private Ui(InputStream in, PrintStream out, boolean isSoundEnabled) {
        this.scanner = new Scanner(in);
        this.out = out;
        this.isSoundEnabled = isSoundEnabled;
    }

    /**
     * Greets the user and points out how to get help and how to leave.
     */
    public void showWelcome() {
        out.println(BANNER);
        out.println(DIVIDER);
        out.println("serangooner at your service. what's up?");
        out.println("to see commands, type 'help'");
        out.println("done? type 'bye' to exit :(");
        out.println(DIVIDER);
    }

    /**
     * Reports what was read from the save file, and says nothing at all when
     * there was neither a task nor a problem worth mentioning.
     *
     * @param report Outcome of reading the save file.
     */
    public void showLoadReport(Storage.LoadResult report) {
        String summary = describe(report);
        if (summary.isEmpty()) {
            return;
        }
        out.println(summary);
        out.println(DIVIDER);
    }

    /**
     * Returns the one line report to show for the given load outcome.
     *
     * @param report Outcome of reading the save file.
     * @return Report to show the user, or an empty string when there is
     *         nothing worth saying.
     */
    private static String describe(Storage.LoadResult report) {
        if (!report.errorMessage().isEmpty()) {
            return report.errorMessage();
        }
        if (report.tasks().isEmpty() && report.skippedLineCount() == 0) {
            return "";
        }
        String summary = "loaded " + report.tasks().size() + " task(s) from your last visit";
        if (report.skippedLineCount() > 0) {
            summary += "; skipped " + report.skippedLineCount() + " unreadable line(s)";
        }
        return summary;
    }

    /**
     * Returns whether the user has entered another command.
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Returns the next command entered by the user.
     */
    public String readCommand() {
        return scanner.nextLine();
    }

    /**
     * Shows the given message to the user.
     *
     * @param message Message to show.
     */
    public void showMessage(String message) {
        out.println(message);
    }

    /**
     * Shows the given error to the user, with a sound to match.
     *
     * @param message Explanation of what went wrong.
     */
    public void showError(String message) {
        playSound();
        out.println(message);
    }

    /**
     * Draws the line that separates one exchange from the next.
     */
    public void showDivider() {
        out.println(DIVIDER);
    }

    /**
     * Says goodbye to the user.
     */
    public void showFarewell() {
        out.println("bye~");
        out.println(DIVIDER);
    }

    /**
     * Plays a funny sound to accompany an invalid command.
     * The sound plays on a separate thread so that it never delays the next
     * prompt, and is skipped when the audio file is absent.
     */
    private void playSound() {
        // Authored with the help of Codex.
        if (!isSoundEnabled || !Files.exists(SOUND_FILE)) {
            return;
        }

        Thread.ofVirtual().start(() -> {
            try {
                new ProcessBuilder("afplay", SOUND_FILE.toString()).start().waitFor();
            } catch (IOException | InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
