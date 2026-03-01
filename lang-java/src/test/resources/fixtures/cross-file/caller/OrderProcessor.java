import java.util.*;

class OrderProcessor {
    void processAll(List<String> orders) {
        for (String order : orders) {
            validate(order);
        }
    }

    void validate(String order) {
        System.out.println("Validating: " + order);
    }
}
