import java.util.ArrayList;
import java.util.List;

/* stores Tasks entered by serangooner */
public class TaskList {
    private static final int MAX_TASKS = 100;
    private final List<Task> tasks = new ArrayList<>(MAX_TASKS);

    public void add(String description) {
        tasks.add(new Task(description));
    }

    public Task mark(int taskNumber) {
        Task task = tasks.get(taskNumber - 1);
        task.markDone();
        return task;
    }

    public Task unmark(int taskNumber) {
        Task task = tasks.get(taskNumber - 1); // 1-based indexing
        task.markNotDone();
        return task;
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
