import java.util.List;
import java.util.Queue;

public class TaskRunner {
    public void runAll(List<TaskGroup> groups) {
        for (int i = 0; i < groups.size(); i++) {
            processGroup(groups.get(i));
        }
    }

    private void processGroup(TaskGroup group) {
        Queue<Task> pending = group.getPending();
        while (!pending.isEmpty()) {
            Task task = pending.poll();
            task.execute();
        }
    }
}
