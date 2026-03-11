import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class InvoiceValidator {
    private static final Pattern INVOICE_PATTERN = Pattern.compile("INV-\\d{8}");

    public List<Invoice> findInvalidInvoices(List<Invoice> invoices) {
        List<Invoice> invalid = new ArrayList<>();
        for (Invoice invoice : invoices) {
            if (!INVOICE_PATTERN.matcher(invoice.getNumber()).matches()) {
                invalid.add(invoice);
            }
        }
        return invalid;
    }
}
