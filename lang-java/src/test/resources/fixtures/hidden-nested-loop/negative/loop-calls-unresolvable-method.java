import java.util.List;

public class ExternalService {
    private final ThirdPartyClient client;

    public ExternalService(ThirdPartyClient client) {
        this.client = client;
    }

    public void syncAll(List<Record> records) {
        for (Record record : records) {
            // client.push() is not resolvable — it's an external dependency
            client.push(record);
        }
    }
}
