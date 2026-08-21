import java.util.LinkedHashMap;
import java.util.Map;

/* stores commands used by serangooner and their assoc. descriptions */
public class CommandLibrary {
    private final Map<String, String> commands = new LinkedHashMap<>();

    public CommandLibrary() {
        commands.put("todo <description>", "add a task without a date or time");
        commands.put("deadline <description> by <date>", "add a task with a deadline");
        commands.put("event <description> from <date> to <date>", "add an event");
        commands.put("list", "view all saved tasks");
        commands.put("mark <number>", "mark a task as done");
        commands.put("unmark <number>", "mark a task as incomplete");
        commands.put("delete <number>", "delete a task");
        commands.put("undo", "undo the last task change");
        commands.put("help", "show commands");
        commands.put("bye", "exit serangooner"); // by codex
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("serangooner commands:");
        int commandNumber = 1;
        for (Map.Entry<String, String> command : commands.entrySet()) {
            output.append(System.lineSeparator())
                    .append(commandNumber)
                    .append(". ")
                    .append(command.getKey())
                    .append(" - ")
                    .append(command.getValue());
            commandNumber++;
        }
        return output.toString();
    }
}
