import java.util.List;
import java.util.ArrayList;

public class EnumValuesIteration {
    List<String> describeAll() {
        List<String> descriptions = new ArrayList<>();
        for (Status status : Status.values()) {
            for (String label : status.getLabels()) {
                descriptions.add(label);
            }
        }
        return descriptions;
    }
}

enum Status {
    ACTIVE, INACTIVE;

    List<String> getLabels() { return null; }
}
