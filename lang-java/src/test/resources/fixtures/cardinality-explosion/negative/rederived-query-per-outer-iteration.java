import java.util.ArrayList;
import java.util.List;

public class ChangeSetCollector {
    public List<String> collectAllChangeSets(List<String> fileNames, ChangeLogSource source) {
        List<String> result = new ArrayList<>();
        for (String fileName : fileNames) {
            List<String> changeSets = source.getChangeSets(fileName);
            for (String changeSet : changeSets) {
                result.add(changeSet);
            }
        }
        return result;
    }

    public interface ChangeLogSource {
        List<String> getChangeSets(String fileName);
    }
}
