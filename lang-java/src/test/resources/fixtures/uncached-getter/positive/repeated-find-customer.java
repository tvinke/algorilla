import java.util.List;

public class InvoiceService {
    private CustomerRepository customerRepository;

    public Invoice createInvoice(String customerId, List<OrderLine> lines) {
        String customerName = findCustomer(customerId).getName();
        String customerEmail = findCustomer(customerId).getEmail();
        return new Invoice(customerName, customerEmail, lines);
    }

    private Customer findCustomer(String id) {
        return customerRepository.findById(id);
    }
}
