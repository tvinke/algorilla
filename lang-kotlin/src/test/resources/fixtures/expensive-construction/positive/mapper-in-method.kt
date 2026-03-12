import com.fasterxml.jackson.databind.ObjectMapper

class SerializationService {
    fun serialize(order: Order): String {
        val mapper = ObjectMapper()
        return mapper.writeValueAsString(order)
    }
}
