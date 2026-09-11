import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class ObsQueueProcessor {
    public void processObsQueue(List<String> ids) {
        Deque<String> obsToUpdate = new ArrayDeque<>();
        for (String id : ids) {
            obsToUpdate.addLast(id);
            obsToUpdate.removeFirst();
        }
    }
}
