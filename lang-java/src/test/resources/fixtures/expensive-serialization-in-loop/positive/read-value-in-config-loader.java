import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigLoader {
    private final ObjectMapper mapper = new ObjectMapper();

    public void loadConfigs(List<String> jsonEntries) {
        for (String json : jsonEntries) {
            Config config = mapper.readValue(json, Config.class);
            applyConfig(config);
        }
    }

    private void applyConfig(Config config) {
        // apply configuration
    }
}
