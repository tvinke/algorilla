import java.lang.reflect.*;
import java.util.*;

public class Inspector {
    public List<String> collectMethodNames(List<Class<?>> classes) {
        List<String> names = new ArrayList<>();
        for (Class<?> clazz : classes) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method m : methods) {
                names.add(m.getName());
            }
        }
        return names;
    }
}
