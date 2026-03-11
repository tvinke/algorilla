import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomerReportService {
    private final ObjectMapper mapper = new ObjectMapper();

    public String generateReport(List<Customer> customers) {
        Report report = new Report(customers);
        return mapper.writeValueAsString(report);
    }
}
