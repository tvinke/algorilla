import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocaleNameValidator {
    public void validateNames(List<String> locales) {
        for (String locale : locales) {
            Set<String> seenNames = new HashSet<>();
            List<String> namesInLocale = getNames(locale);
            for (String name : namesInLocale) {
                if (!seenNames.add(name)) {
                    throw new IllegalStateException("Duplicate name: " + name);
                }
            }
        }
    }

    private List<String> getNames(String locale) {
        return List.of();
    }
}
