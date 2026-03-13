class ReportService {
    void generateReport(List orders) {
        for (Object order : orders) {
            println(order.total);
        }
        for (Object order : orders) {
            println(order.customer);
        }
    }
}
