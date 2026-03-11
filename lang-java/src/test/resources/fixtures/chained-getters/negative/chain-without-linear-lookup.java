import java.util.Map;

public class CustomerProfileService {
    private Map<String, Customer> customerMap;
    private Map<String, Address> addressMap;

    public String getCustomerCity(String customerId) {
        Customer customer = getCustomer(customerId);
        Address address = getAddress(customer.getAddressId());
        return address.getCity();
    }

    private Customer getCustomer(String id) {
        return customerMap.get(id);
    }

    private Address getAddress(String id) {
        return addressMap.get(id);
    }
}
