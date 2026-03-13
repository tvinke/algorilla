class ValidationService {
    void validateAll(List items) {
        for (Object item : items) {
            validator.validate(item);
        }
    }
}
