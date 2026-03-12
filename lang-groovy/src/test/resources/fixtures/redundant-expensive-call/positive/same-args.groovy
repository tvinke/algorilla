class TextNormalizer {
    String normalize(String input) {
        String a = input.convertPattern("\\s+", " ");
        String b = input.convertPattern("\\s+", " ");
        return a + " " + b;
    }
}
