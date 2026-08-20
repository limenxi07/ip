import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Serangooner {
    public static void main(String[] args) {
        String banner = "  ____   U _____ u   ____        _      _   _     ____    U  ___ u   U  ___ u  _   _   U _____ u   ____     \n"
                + " / __\"| u\\| ___\"|/U |  _\"\\ u U  /\"\\  u | \\ |\"| U /\"___|u   \\/\"_ \\/    \\/\"_ \\/ | \\ |\"|  \\| ___\"|/U |  _\"\\ u  \n"
                + "<\\___ \\/  |  _|\"   \\| |_) |/  \\/ _ \\/ <|  \\| |>\\| |  _ /   | | | |    | | | |<|  \\| |>  |  _|\"   \\| |_) |/  \n"
                + " u___) |  | |___    |  _ <    / ___ \\ U| |\\  |u | |_| |.-,_| |_| |.-,_| |_| |U| |\\  |u  | |___    |  _ <    \n"
                + " |____/>> |_____|   |_| \\_\\  /_/   \\_\\ |_| \\_|   \\____| \\_)-\\___/  \\_)-\\___/  |_| \\_|   |_____|   |_| \\_\\   \n"
                + "  )(  (__)<<   >>   //   \\\\_  \\\\    >> ||   \\\\,-._)(|_       \\\\         \\\\    ||   \\\\,-.<<   >>   //   \\\\_  \n"
                + " (__)    (__) (__) (__)  (__)(__)  (__)(_\")  (_/(__)__)     (__)       (__)   (_\")  (_/(__) (__) (__)  (__)  \n";
        String divider = "━━━ . °‧ \uD80C\uDD9D \uD80C\uDD9F \uD80C\uDD9E ·｡";

        /* intro */
        System.out.println(banner);
        System.out.println("serangooner at your service. what's up?");
        System.out.println("add items to your list, type 'list' to view");
        System.out.println("type 'bye' to end chat :(");
        System.out.println(divider);

        List<String> tasks = new ArrayList<>(100);
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
            if (command.equals("list")) {
                System.out.println("your list");
                for (int i = 0; i < tasks.size(); i++) {
                    System.out.println(" " + (i + 1) + ". " + tasks.get(i));
                }
            } else { // default command: add task
                tasks.add(command);
                System.out.println("added to list: " + command);
            }
            System.out.println(divider);
        }
    }
}
