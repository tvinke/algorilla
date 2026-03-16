class SummaryService {
    void summarize(List orders, List returns) {
        for (Object order : orders) {
            println(order.total);
        }
        for (Object ret : returns) {
            println(ret.amount);
        }
    }
}
