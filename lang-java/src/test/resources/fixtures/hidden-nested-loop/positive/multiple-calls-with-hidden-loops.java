import java.util.List;

public class ShippingService {
    public void processShipments(List<Shipment> shipments) {
        for (Shipment shipment : shipments) {
            validateItems(shipment);
            calculateWeights(shipment);
        }
    }

    private void validateItems(Shipment shipment) {
        for (Item item : shipment.getItems()) {
            item.validate();
        }
    }

    private void calculateWeights(Shipment shipment) {
        for (Item item : shipment.getItems()) {
            item.weigh();
        }
    }
}
