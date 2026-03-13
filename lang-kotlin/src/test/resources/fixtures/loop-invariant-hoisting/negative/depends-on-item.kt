class ItemProcessor {
    fun validateAll(items: List<Item>, validator: Validator) {
        for (item in items) {
            validator.validate(item)
        }
    }
}
