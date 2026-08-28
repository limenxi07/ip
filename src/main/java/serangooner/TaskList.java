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

    /**
     * Adds the given todo to the list.
     *
     * @param todo Todo to add.
     * @return Confirmation message naming the task that was added.
     */
    public String addTodo(Todo todo) {
        return add(todo, "todo");
    }

    /**
     * Adds the given deadline to the list.
     *
     * @param deadline Deadline to add.
     * @return Confirmation message naming the task that was added.
     */
    public String addDeadline(Deadline deadline) {
        return add(deadline, "deadline");
    }

    /**
     * Adds the given event to the list.
     *
     * @param event Event to add.
     * @return Confirmation message naming the task that was added.
     */
    public String addEvent(Event event) {
        return add(event, "event");
    }

    /**
     * Adds the given task to the end of the list.
     *
     * @param task Task to add.
     * @param label Name this kind of task goes by in the confirmation message.
     * @return Confirmation message naming the task that was added.
     */
    private String add(Task task, String label) {
        tasks.add(task);
        undoActions.push(() -> tasks.remove(task));
        return "added " + label + ": " + task + System.lineSeparator()
                + "you now have " + size() + " pending task(s) :c";
    }

    /**
     * Marks the task at the given position as done.
     *
     * @param taskNumber Position of the task in the list, counting from one.
     * @return Confirmation message naming the task that was marked.
     * @throws SerangoonerException If no task holds that position.
     */
    public String mark(int taskNumber) {
        Task task = getTask(taskNumber, "MARK");
        boolean wasDone = task.isDone();
        task.markDone();
        undoActions.push(() -> setDone(task, wasDone));
        return "marked task as done: " + task;
    }

    /**
     * Marks the task at the given position as incomplete.
     *
     * @param taskNumber Position of the task in the list, counting from one.
     * @return Confirmation message naming the task that was unmarked.
     * @throws SerangoonerException If no task holds that position.
     */
    public String unmark(int taskNumber) {
        Task task = getTask(taskNumber, "UNMARK");
        boolean wasDone = task.isDone();
        task.markNotDone();
        undoActions.push(() -> setDone(task, wasDone));
        return "marked task as incomplete: " + task;
    }

    /**
     * Deletes the task at the given position.
     *
     * @param taskNumber Position of the task in the list, counting from one.
     * @return Confirmation message naming the task that was deleted.
     * @throws SerangoonerException If no task holds that position.
     */
    public String delete(int taskNumber) {
        Task task = getTask(taskNumber, "DELETE");
        tasks.remove(taskNumber - 1);
        undoActions.push(() -> tasks.add(taskNumber - 1, task));
        return "deleted task: " + task;
    }

    /**
     * Returns the task at the given position.
     *
     * @param taskNumber Position of the task in the list, counting from one.
     * @param label Name of the command, for use in the error message.
     * @return Task holding that position.
     * @throws SerangoonerException If no task holds that position.
     */
    private Task getTask(int taskNumber, String label) {
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new SerangoonerException(label + " FAILED. task number must be between 1 and "
                    + tasks.size());
        }
        return tasks.get(taskNumber - 1);
    }

    /**
     * Sets whether the given task is complete.
     *
     * @param task Task to update.
     * @param isDone True to mark the task as done, false to mark it as incomplete.
     */
    private static void setDone(Task task, boolean isDone) {
        if (isDone) {
            task.markDone();
        } else {
            task.markNotDone();
        }
    }

    /**
     * Returns the tasks falling on or between the given dates.
     *
     * @param start First date of the range, inclusive.
     * @param end Last date of the range, inclusive.
     * @return Listing of the matching tasks, keeping the numbers they have in
     *         the full list.
     */
    public String occurringOn(LocalDate start, LocalDate end) {
        String range = start.equals(end)
                ? "on " + TaskDateTime.format(start)
                : "between " + TaskDateTime.format(start) + " and " + TaskDateTime.format(end);
        return listTasks("tasks " + range, "you have nothing " + range + " :D",
                task -> task.isWithin(start, end));
    }

    /**
     * Returns the number of tasks currently stored.
     */
    public int size() {
        return tasks.size();
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
