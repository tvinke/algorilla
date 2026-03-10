import java.util.List;

public class MatrixService {
    public void processMatrix(List<Row> rows) {
        for (Row row : rows) {
            for (Cell cell : row.getCells()) {
                expandCell(cell);
            }
        }
    }

    private void expandCell(Cell cell) {
        for (SubCell sub : cell.getSubCells()) {
            sub.render();
        }
    }
}
