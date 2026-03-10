import java.util.List;
import java.time.LocalDate;

public class DateInForEach {
    public void processItems(List<String> dates) {
        dates.forEach(d -> {
            LocalDate parsed = LocalDate.parse(d);
            System.out.println(parsed.getYear());
        });
    }
}
