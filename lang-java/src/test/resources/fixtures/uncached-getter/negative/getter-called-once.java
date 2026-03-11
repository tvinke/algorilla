public class ShippingService {
    private AddressService addressService;

    public String getShippingLabel(String customerId) {
        Address address = addressService.getAddress(customerId);
        return address.getStreet() + ", " + address.getCity();
    }
}
