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
                System.out.println(divider);
                System.out.println("bye~");
                System.out.println(divider);
                break; // exit
            }

            System.out.println(divider);
            if (command.equals("help")) {
                System.out.println(commands);
            } else if (command.equals("list")) {
                System.out.println(tasks);
            } else if (command.equals("undo")) {
                System.out.println(tasks.undo() ? "undid your last edit" : "there's nothing to undo >:(");
            } else if (command.startsWith("mark ")) {
                Task task = tasks.mark(Integer.parseInt(command.substring(5)));
                System.out.println("marked task as done: " + task);
            } else if (command.startsWith("unmark ")) {
                Task task = tasks.unmark(Integer.parseInt(command.substring(7)));
                System.out.println("marked task as incomplete: " + task);
            } else if (command.startsWith("delete ")) {
                Task task = tasks.delete(Integer.parseInt(command.substring(7)));
                System.out.println("deleted task: " + task);
            } else if (command.startsWith("todo ")) {
                String description = command.substring(5);
                Task task = tasks.addTodo(description);
                System.out.println("added todo: " + task);
                System.out.println("you now have " + tasks.size() + " pending tasks :c"); // maybe combine all these print statements later
            } else if (command.startsWith("deadline ")) {
                try {
                    Task task = tasks.addDeadline(command);
                    System.out.println("added deadline: " + task);
                    System.out.println("you now have " + tasks.size() + " pending tasks :c");
                } catch (IllegalArgumentException exception) {
                    System.out.println(exception.getMessage());
                }
            } else if (command.startsWith("event ")) {
                try {
                    Task task = tasks.addEvent(command);
                    System.out.println("added event: " + task);
                    System.out.println("you now have " + tasks.size() + " pending tasks :c");
                } catch (IllegalArgumentException exception) {
                    System.out.println(exception.getMessage());
                }
            } else { // default command: echo
                System.out.println("echo: " + command);
            }
            System.out.println(divider);
        }
    }
}
