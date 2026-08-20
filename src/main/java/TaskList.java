import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/* stores Tasks entered by serangooner */
public class TaskList {
    private static final int MAX_TASKS = 100;
    private final List<Task> tasks = new ArrayList<>(MAX_TASKS);
    private final Deque<Runnable> undoActions = new ArrayDeque<>();

    public void add(String description) {
        Task task = new Task(description);
        tasks.add(task);
        undoActions.push(() -> tasks.remove(task));
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

    public boolean undo() { // undo authored by codex
        if (undoActions.isEmpty()) {
            return false;
        }
        undoActions.pop().run();
        return true;
    }

    @Override
    public String toString() { // string representation with completion status
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
