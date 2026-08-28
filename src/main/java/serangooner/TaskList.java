package serangooner;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;

/**
 * Stores and edits the tasks entered by the user.
 * The list lives entirely in memory and knows nothing of where its tasks
 * came from, so writing them back to disk is left to whoever holds both
 * this list and a {@link Storage}.
 * Every edit pushes an action that reverses it, so the most recent edit
 * can be undone.
 */
public class TaskList {
    private static final int MAX_TASKS = 100;
    private final List<Task> tasks = new ArrayList<>(MAX_TASKS);
    // Undo stack authored with the help of Codex.
    private final Deque<Runnable> undoActions = new ArrayDeque<>();

    /**
     * Constructs an empty task list.
     */
    public TaskList() {
        this(List.of());
    }

    /**
     * Constructs a task list holding the given tasks.
     *
     * @param tasks Tasks the list starts out with, in the order they are shown.
     */
    public TaskList(List<Task> tasks) {
        this.tasks.addAll(tasks);
    }

    /**
     * Returns the tasks in the order they are shown, as a view that cannot be
     * edited through.
     */
    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    // Methods that parse a raw user command.

    /**
     * Adds a todo described by the given command.
     *
     * @param command Command in the form "todo &lt;description&gt;".
     * @return Confirmation message naming the task that was added.
     * @throws SerangoonerException If the description is missing.
     */
    public String addTodo(String command) {
        String description = command.length() > 5 ? command.substring(5).trim() : "";
        if (description.isEmpty()) {
            throw new SerangoonerException("pls name ur task: todo <description>");
        }
        Task task = new Todo(description);
        tasks.add(task);
        undoActions.push(() -> tasks.remove(task));
        return "added todo: " + task + System.lineSeparator()
                + "you now have " + size() + " pending task(s) :c";
    }

    /**
     * Adds a deadline described by the given command.
     *
     * @param command Command in the form "deadline &lt;description&gt; by &lt;date&gt;".
     * @return Confirmation message naming the task that was added.
     * @throws SerangoonerException If the command does not follow that form.
     */
    public String addDeadline(String command) {
        Task task = new Deadline(command);
        tasks.add(task);
        undoActions.push(() -> tasks.remove(task));
        return "added deadline: " + task + System.lineSeparator()
                + "you now have " + size() + " pending task(s) :c";
    }

    /**
     * Adds an event described by the given command.
     *
     * @param command Command in the form "event &lt;description&gt; from &lt;date&gt; to &lt;date&gt;".
     * @return Confirmation message naming the task that was added.
     * @throws SerangoonerException If the command does not follow that form.
     */
    public String addEvent(String command) {
        Task task = new Event(command);
        tasks.add(task);
        undoActions.push(() -> tasks.remove(task));
        return "added event: " + task + System.lineSeparator()
                + "you now have " + size() + " pending task(s) :c";
    }

    /**
     * Marks the task named by the given command as done.
     *
     * @param command Command in the form "mark &lt;number&gt;".
     * @return Confirmation message naming the task that was marked.
     * @throws SerangoonerException If the task number is missing or out of range.
     */
    public String mark(String command) {
        return updateStatus(command, "mark", true);
    }

    /**
     * Marks the task named by the given command as incomplete.
     *
     * @param command Command in the form "unmark &lt;number&gt;".
     * @return Confirmation message naming the task that was unmarked.
     * @throws SerangoonerException If the task number is missing or out of range.
     */
    public String unmark(String command) {
        return updateStatus(command, "unmark", false);
    }

    /**
     * Returns the outcome of applying the given completion status to the task
     * named by the command.
     *
     * @param command Command in the form "&lt;commandName&gt; &lt;number&gt;".
     * @param commandName Keyword that starts the command.
     * @param isMarkingDone True to mark the task as done, false to mark it as incomplete.
     * @return Confirmation message naming the task that changed.
     * @throws SerangoonerException If the task number is missing or out of range.
     */
    private String updateStatus(String command, String commandName, boolean isMarkingDone) {
        int taskNumber = parseTaskNumber(command, commandName);
        Task task = isMarkingDone ? mark(taskNumber) : unmark(taskNumber);
        return isMarkingDone ? "marked task as done: " + task : "marked task as incomplete: " + task;
    }

    /**
     * Returns the task number given as the sole argument of the command.
     *
     * @param command Command in the form "&lt;commandName&gt; &lt;number&gt;".
     * @param commandName Keyword that starts the command.
     * @return Position of the task in the list, counting from one.
     * @throws SerangoonerException If the number is missing, not a number, or out of range.
     */
    private int parseTaskNumber(String command, String commandName) {
        String argument = command.length() > commandName.length()
                ? command.substring(commandName.length()).trim() : "";
        String label = commandName.toUpperCase();
        if (argument.isEmpty()) {
            throw new SerangoonerException(label + " FAILED. pls use format: " + commandName + " <number>");
        }

        final int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument);
        } catch (NumberFormatException exception) {
            throw new SerangoonerException(label + " FAILED. pls give a valid task number", exception);
        }
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new SerangoonerException(label + " FAILED. task number must be between 1 and "
                    + tasks.size());
        }
        return taskNumber;
    }

    /**
     * Deletes the task named by the given command.
     *
     * @param command Command in the form "delete &lt;number&gt;".
     * @return Confirmation message naming the task that was deleted.
     * @throws SerangoonerException If the task number is missing or out of range.
     */
    public String delete(String command) {
        return "deleted task: " + delete(parseTaskNumber(command, "delete"));
    }

    /**
     * Returns the tasks falling on the date, or within the range of dates,
     * named by the given command.
     *
     * @param command Command in the form "on &lt;date&gt;" or "on &lt;date&gt; to &lt;date&gt;".
     * @return Listing of the matching tasks, keeping the numbers they have in
     *         the full list.
     * @throws SerangoonerException If a date is missing, is not in an accepted
     *         format, or the range ends before it starts.
     */
    public String occurringOn(String command) {
        String argument = command.length() > 2 ? command.substring(2).trim() : "";
        if (argument.isEmpty()) {
            throw new SerangoonerException("pls give a date: on <date> [to <date>]. "
                    + TaskDateTime.FORMAT_HINT);
        }

        int toIndex = argument.indexOf(" to ");
        LocalDate start = TaskDateTime.parseDate(
                toIndex < 0 ? argument : argument.substring(0, toIndex));
        LocalDate end = toIndex < 0 ? start
                : TaskDateTime.parseDate(argument.substring(toIndex + 4));
        if (end.isBefore(start)) {
            throw new SerangoonerException("that range ends before it starts o.O");
        }

        String range = start.equals(end)
                ? "on " + TaskDateTime.format(start)
                : "between " + TaskDateTime.format(start) + " and " + TaskDateTime.format(end);
        return listTasks("tasks " + range, "you have nothing " + range + " :D",
                task -> task.isWithin(start, end));
    }

    // Methods that act on tasks directly.

    /**
     * Returns the number of tasks currently stored.
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Marks the task at the given position as done.
     *
     * @param taskNumber Position of the task in the list, counting from one.
     * @return Task that was marked.
     */
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

    /**
     * Marks the task at the given position as incomplete.
     *
     * @param taskNumber Position of the task in the list, counting from one.
     * @return Task that was unmarked.
     */
    public Task unmark(int taskNumber) {
        Task task = tasks.get(taskNumber - 1);
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

    /**
     * Deletes the task at the given position.
     *
     * @param taskNumber Position of the task in the list, counting from one.
     * @return Task that was deleted.
     */
    public Task delete(int taskNumber) {
        Task task = tasks.remove(taskNumber - 1);
        undoActions.push(() -> tasks.add(taskNumber - 1, task));
        return task;
    }

    /**
     * Reverses the most recent edit made to the list.
     *
     * @return True if an edit was undone, false if there was nothing to undo.
     */
    public boolean undo() {
        if (undoActions.isEmpty()) {
            return false;
        }
        undoActions.pop().run();
        return true;
    }

    /**
     * Returns a numbered listing of the tasks that the given filter accepts.
     * A task keeps the number it has in the full list, so that a number read
     * off any listing stays usable with mark, unmark and delete.
     *
     * @param heading Line introducing the listing.
     * @param emptyMessage Message to return when the filter accepts no task.
     * @param filter Decides which tasks appear in the listing.
     * @return Listing to show the user.
     */
    private String listTasks(String heading, String emptyMessage, Predicate<Task> filter) {
        StringBuilder output = new StringBuilder(heading);
        boolean hasMatch = false;
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (!filter.test(task)) {
                continue;
            }

            hasMatch = true;
            output.append(System.lineSeparator())
                    .append(" ")
                    .append(i + 1)
                    .append(". ")
                    .append(task);
        }
        return hasMatch ? output.toString() : emptyMessage;
    }

    @Override
    public String toString() {
        return listTasks("your list", "your list is empty T-T add something with 'todo ...'",
                task -> true);
    }
}
