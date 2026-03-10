import java.util.List;
import java.util.Iterator;

public class EventProcessor {
    public void drainQueue(Iterator<Event> events) {
        while (events.hasNext()) {
            Event event = events.next();
            notifyListeners(event);
        }
    }

    private void notifyListeners(Event event) {
        for (Listener listener : event.getListeners()) {
            listener.onEvent(event);
        }
    }
}
