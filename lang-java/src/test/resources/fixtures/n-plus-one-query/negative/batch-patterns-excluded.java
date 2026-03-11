import java.util.List;
import java.util.Set;

public class BatchService {
    private OrderRepository orderRepository;

    public void batchProcess(List<String> statuses, Set<Long> ids) {
        for (String status : statuses) {
            List orders = orderRepository.findAllByStatus(status);
            List byStatus = orderRepository.findByStatusIn(ids);
            List range = orderRepository.findByDateBetween(null, null);
            List like = orderRepository.findByNameContaining(status);
        }
    }
}

interface OrderRepository {
    List findAllByStatus(String status);
    List findByStatusIn(Set ids);
    List findByDateBetween(Object start, Object end);
    List findByNameContaining(String name);
}
