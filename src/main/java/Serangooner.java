import java.util.Scanner;

public class Serangooner {
    public static void main(String[] args) {
        /* assets */
        String banner = "  ____   U _____ u   ____        _      _   _     ____    U  ___ u   U  ___ u  _   _   U _____ u   ____     \n"
                + " / __\"| u\\| ___\"|/U |  _\"\\ u U  /\"\\  u | \\ |\"| U /\"___|u   \\/\"_ \\/    \\/\"_ \\/ | \\ |\"|  \\| ___\"|/U |  _\"\\ u  \n"
                + "<\\___ \\/  |  _|\"   \\| |_) |/  \\/ _ \\/ <|  \\| |>\\| |  _ /   | | | |    | | | |<|  \\| |>  |  _|\"   \\| |_) |/  \n"
                + " u___) |  | |___    |  _ <    / ___ \\ U| |\\  |u | |_| |.-,_| |_| |.-,_| |_| |U| |\\  |u  | |___    |  _ <    \n"
                + " |____/>> |_____|   |_| \\_\\  /_/   \\_\\ |_| \\_|   \\____| \\_)-\\___/  \\_)-\\___/  |_| \\_|   |_____|   |_| \\_\\   \n"
                + "  )(  (__)<<   >>   //   \\\\_  \\\\    >> ||   \\\\,-._)(|_       \\\\         \\\\    ||   \\\\,-.<<   >>   //   \\\\_  \n"
                + " (__)    (__) (__) (__)  (__)(__)  (__)(_\")  (_/(__)__)     (__)       (__)   (_\")  (_/(__) (__) (__)  (__)  \n";
        String divider = "━━━ . °‧ \uD80C\uDD9D \uD80C\uDD9F \uD80C\uDD9E ·｡";
        CommandLibrary commands = new CommandLibrary();

        /* intro */
        System.out.println(banner);
        System.out.println(divider);
        System.out.println("serangooner at your service. what's up?");
        System.out.println("to see commands, type 'help'");
        System.out.println("done? type 'bye' to exit :(");
        System.out.println(divider);

        /* user input */
        TaskList tasks = new TaskList();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            if (command.equals("bye")) {
                //System.out.println(divider);
                System.out.println("bye~");
                System.out.println(divider);
                break; // exit
            }

            //System.out.println(divider);
            if (command.equals("help")) {
                System.out.println(commands);
            } else if (command.equals("list")) {
                System.out.println(tasks);
            } else if (command.equals("undo")) {
                System.out.println(tasks.undo() ? "undid your last edit" : "there's nothing to undo >:(");
            } else if (command.equals("mark") || command.startsWith("mark ")) {
                System.out.println(tasks.mark(command));
            } else if (command.equals("unmark") || command.startsWith("unmark ")) {
                System.out.println(tasks.unmark(command));
            } else if (command.equals("delete") || command.startsWith("delete ")) {
                System.out.println(tasks.delete(command));
            } else if (command.equals("todo") || command.startsWith("todo ")) {
                System.out.println(tasks.addTodo(command));
            } else if (command.equals("deadline") || command.startsWith("deadline ")) {
                try {
                    System.out.println(tasks.addDeadline(command));
                } catch (IllegalArgumentException exception) {
                    System.out.println(exception.getMessage());
                }
            } else if (command.equals("event") || command.startsWith("event ")) {
                try {
                    System.out.println(tasks.addEvent(command));
                } catch (IllegalArgumentException exception) {
                    System.out.println(exception.getMessage());
                }
            } else { // reject command L
                System.out.println("invalid command :/ if you don't know what you're doing, pls type 'help' for the command library .-.");
            }
            System.out.println(divider);
        }
    }
}
