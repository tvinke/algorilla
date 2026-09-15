public class LoanResource {
    public Result update(Long id, RequestContext ctx, String body) {
        return update(id, ExternalId.empty(), body);
    }

    private Result update(Long id, ExternalId externalId, String body) {
        return process(id, externalId, body);
    }
}
