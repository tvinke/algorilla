import java.util.*;

class OrderService {
    void sortByPriority(List orders, List priorities) {
        orders.sort((a, b) -> priorities.indexOf(a) - priorities.indexOf(b));
    }
}
