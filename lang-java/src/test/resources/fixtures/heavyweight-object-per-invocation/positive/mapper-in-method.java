public class SerializationService {
    public String serialize(Order order) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(order);
    }
}
