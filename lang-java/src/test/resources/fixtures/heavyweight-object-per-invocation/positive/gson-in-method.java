public class JsonHelper {
    public Object parse(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Object.class);
    }
}
