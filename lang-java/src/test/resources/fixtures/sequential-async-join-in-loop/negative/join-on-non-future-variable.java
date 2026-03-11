import java.util.List;
import java.util.StringJoiner;

public class InvoiceFormatter {
    public String formatInvoiceLines(List<InvoiceLine> lines) {
        StringJoiner result = new StringJoiner("\n");
        for (InvoiceLine line : lines) {
            result.add(line.getDescription() + ": " + line.getAmount());
        }
        return result.toString();
    }
}
