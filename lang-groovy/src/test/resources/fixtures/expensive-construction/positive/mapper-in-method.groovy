import com.fasterxml.jackson.databind.ObjectMapper;

class SerializationService {
    String serialize(Object order) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(order);
    }
}
