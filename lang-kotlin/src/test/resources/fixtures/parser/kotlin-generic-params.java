import java.util.List;
import java.util.HashSet;

class GenericParamProcessor {
    fun filterByIds(ids: List<String>, active: HashSet<String>) {
        ids.forEach(id -> {
            if (active.contains(id)) {
                System.out.println(id);
            }
        });
    }
}
