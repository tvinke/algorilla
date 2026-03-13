import java.util.*;
import java.util.stream.*;

public class ReportService {
    public void generateReport(List<Transaction> transactions) {
        Map<String, List<Transaction>> grouped = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCategory));
    }
}
