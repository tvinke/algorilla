import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

public class LoanCycleCalculator {
    private Product product;

    public Integer resolveDefaultPrincipal(Map.Entry<Long, Integer> mapEntry) {
        Integer loanCycleNumber = mapEntry.getValue();
        Collection<BigDecimal> principalVariationsForBorrowerCycle = product.getPrincipalVariationsForBorrowerCycle();
        return fetchLoanCycleDefaultValue(principalVariationsForBorrowerCycle, loanCycleNumber);
    }

    private Integer fetchLoanCycleDefaultValue(Collection<BigDecimal> variations, Integer cycleNumber) {
        for (BigDecimal variation : variations) {
            if (cycleNumber != null) {
                return variation.intValue();
            }
        }
        return null;
    }
}
