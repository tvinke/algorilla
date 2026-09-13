import java.util.Map;

public class Sub extends Base {
    protected String resolveKey(Map<String, String> vars) {
        if (super.resolveKey(vars) != null) {
            return super.resolveKey(vars);
        }
        return "default";
    }
}
