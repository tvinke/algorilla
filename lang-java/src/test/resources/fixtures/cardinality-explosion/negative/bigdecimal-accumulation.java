class BigDecimalAccumulation {
    BigDecimal calculate(List<Item> items, List<String> types) {
        BigDecimal commission = BigDecimal.ZERO;
        for (Item item : items) {
            for (String type : types) {
                commission = commission.add(item.getValue());
            }
        }
        return commission;
    }
}
