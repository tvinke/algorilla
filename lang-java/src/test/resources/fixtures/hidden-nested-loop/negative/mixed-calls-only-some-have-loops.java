import java.util.List;

public class InvoiceService {
    public void processInvoices(List<Invoice> invoices) {
        for (Invoice invoice : invoices) {
            log(invoice);
            // only format() has no loop — log() and validate() have no loop
        }
    }

    private void log(Invoice invoice) {
        System.out.println(invoice.getId());
    }

    private String format(Invoice invoice) {
        return invoice.toString();
    }
}
