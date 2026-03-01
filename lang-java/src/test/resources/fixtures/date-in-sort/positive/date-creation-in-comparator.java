import java.util.*;

class EventService {
    void sortByDate(List events) {
        events.sort((a, b) -> new Date(a.toString()).compareTo(new Date(b.toString())));
    }
}
