import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/* stores Tasks entered by serangooner */
public class TaskList {
    private static final int MAX_TASKS = 100;
    private final List<Task> tasks = new ArrayList<>(MAX_TASKS);
    private final Deque<Runnable> undoActions = new ArrayDeque<>(); // undo authored by codex

    /**
     * METHODS TO PARSE USER INPUT
     * error handling by codex
     */
    public String addTodo(String command) { // by codex
        String description = command.length() > 5 ? command.substring(5).trim() : "";
        if (description.isEmpty()) {
            return "pls name ur task: todo <description>";
        }
        Task task = new Todo(description);
        tasks.add(task);
        undoActions.push(() -> tasks.remove(task));
        return "added todo: " + task + System.lineSeparator()
                + "you now have " + size() + " pending task(s) :c";
    }

    public String addDeadline(String command) {
        Task task = new Deadline(command);
        tasks.add(task);
        undoActions.push(() -> tasks.remove(task));
        return "added deadline: " + task + System.lineSeparator()
                + "you now have " + size() + " pending task(s) :c";
    }

    public String addEvent(String command) {
        Task task = new Event(command);
        tasks.add(task);
        undoActions.push(() -> tasks.remove(task));
        return "added event: " + task + System.lineSeparator()
                + "you now have " + size() + " pending task(s) :c";
    }

    public String mark(String command) {
        return updateStatus(command, "mark", true);
    }

    public String unmark(String command) {
        return updateStatus(command, "unmark", false);
    }

    private String updateStatus(String command, String commandName, boolean markDone) {
        String argument = command.length() > commandName.length()
                ? command.substring(commandName.length()).trim() : "";
        String label = commandName.toUpperCase();
        if (argument.isEmpty()) {
            return label + " FAILED. pls use format: " + commandName + " <number>";
        }

        final int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument);
        } catch (NumberFormatException exception) {
            return label + " FAILED. pls give a valid task number";
        }
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            return label + " FAILED. task number must be between 1 and " + tasks.size();
        }

        Task task = markDone ? mark(taskNumber) : unmark(taskNumber);
        return markDone ? "marked task as done: " + task : "marked task as incomplete: " + task;
    }

    public String delete(String command) {
        String argument = command.length() > 6 ? command.substring(6).trim() : "";
        if (argument.isEmpty()) {
            return "DELETE FAILED. pls use format: delete <number>";
        }

        final int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument);
        } catch (NumberFormatException exception) {
            return "DELETE FAILED. pls give a valid task number";
        }
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            return "DELETE FAILED. task number must be between 1 and " + tasks.size();
        }
        return "deleted task: " + delete(taskNumber);
    }

    /**
     * METHODS TO HANDLE TASKS
     */
    public int size() {
        return tasks.size();
    }

    public Task mark(int taskNumber) {
        Task task = tasks.get(taskNumber - 1);
        boolean wasDone = task.isDone();
        task.markDone();
        undoActions.push(() -> {
            if (wasDone) {
                task.markDone();
            } else {
                task.markNotDone();
            }
        });
        return task;
    }

    public Task unmark(int taskNumber) {
        Task task = tasks.get(taskNumber - 1); // 1-based indexing
        boolean wasDone = task.isDone();
        task.markNotDone();
        undoActions.push(() -> {
            if (wasDone) {
                task.markDone();
            } else {
                task.markNotDone();
            }
        });
        return task;
    }

    public Task delete(int taskNumber) {
        Task task = tasks.remove(taskNumber - 1);
        undoActions.push(() -> tasks.add(taskNumber - 1, task));
        return task;
    }

    public boolean undo() { // undo authored by codex (for fun feature)
        if (undoActions.isEmpty()) {
            return false;
        }
        undoActions.pop().run();
        return true;
    }

    @Override
    public String toString() { // string representation with completion status
        if (size() == 0) {
            return "your list is empty T-T add something with 'todo ...'";
        }
        StringBuilder output = new StringBuilder("your list");
        for (int i = 0; i < tasks.size(); i++) {
            output.append(System.lineSeparator())
                    .append(" ")
                    .append(i + 1)
                    .append(". ")
                    .append(tasks.get(i)); // done by codex
        }
        return output.toString();
    }

}
