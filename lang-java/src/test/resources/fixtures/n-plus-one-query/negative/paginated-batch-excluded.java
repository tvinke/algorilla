import java.util.List;

public class PaginatedBatchService {
    private RuleRepository ruleRepository;
    private ProductRepository productRepository;

    // findFirst<N>By is Spring Data pagination — returns a List, not a single record
    void scanRules() {
        long maxId = 0;
        boolean hasMore = true;
        while (hasMore) {
            List<Object> rules = ruleRepository.findFirst500ByIdGreaterThanOrderByIdAsc(maxId);
            hasMore = rules.size() == 500;
        }
    }

    // findTop<N>By is equivalent Spring Data pagination syntax
    void topPerCategory(List<String> categories) {
        for (String category : categories) {
            List<Object> top = productRepository.findTop10ByCategoryOrderByRatingDesc(category);
        }
    }
}

interface RuleRepository {
    List<Object> findFirst500ByIdGreaterThanOrderByIdAsc(long id);
}

interface ProductRepository {
    List<Object> findTop10ByCategoryOrderByRatingDesc(String category);
}
