import java.io.File;
import java.util.List;

public class FilesDeleteInLoop {
    public void cleanup(List<File> files) {
        for (File file : files) {
            file.delete();
        }
    }
}
