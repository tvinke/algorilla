public class Decoder {
    public void decode(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String decodedKey = URLDecoder.decode(key, "UTF-8");
            String decodedValue = URLDecoder.decode(value, "UTF-8");
            System.out.println(decodedKey + "=" + decodedValue);
        }
    }
}
