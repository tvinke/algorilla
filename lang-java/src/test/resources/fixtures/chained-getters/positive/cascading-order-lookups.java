import java.util.List;

public class OrderReportService {
    private List<Order> orders;
    private List<Customer> customers;
    private List<Address> addresses;

    public String generateShippingLabel(String orderId) {
        Order order = getOrder(orderId);
        Customer customer = getCustomer(order);
        Address address = getAddress(customer);
        return customer.getName() + "\n" + address.getStreet();
    }

    private Order getOrder(String orderId) {
        for (Order o : orders) {
            if (o.getId().equals(orderId)) return o;
        }
        return null;
    }

    private Customer getCustomer(Order order) {
        for (Customer c : customers) {
            if (c.getId().equals(order.getCustomerId())) return c;
        }
        return null;
    }

    private Address getAddress(Customer customer) {
        for (Address a : addresses) {
            if (a.getId().equals(customer.getAddressId())) return a;
        }
        return null;
    }
}
