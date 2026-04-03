import java.util.List;
import java.util.stream.Collectors;

// Per-element property access: the inner collection differs per iteration,
// so total work is O(Σ |list_i|), not O(n × m) on a shared collection.
public class PerElementPropertyAccess {

    static class MoveLine {
        List<InvoiceTerm> getInvoiceTermList() { return List.of(); }
    }

    static class InvoiceTerm {
        boolean getIsHoldBack() { return false; }
    }

    // allMatch/noneMatch on a per-element property should NOT be flagged
    void processLines(List<MoveLine> moveLineList) {
        for (MoveLine moveLine : moveLineList) {
            if (moveLine.getInvoiceTermList().stream().allMatch(t -> !t.getIsHoldBack())) {
                // process
            }
        }
    }

    // filter/collect on a per-element property should NOT be flagged
    void collectFromLines(List<MoveLine> moveLineList) {
        for (MoveLine moveLine : moveLineList) {
            List<InvoiceTerm> active = moveLine.getInvoiceTermList().stream()
                .filter(t -> !t.getIsHoldBack())
                .collect(Collectors.toList());
        }
    }
}
