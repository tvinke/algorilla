class EmailValidator {
    fun validateEmails(emails: List<String>): List<Boolean> {
        val results = mutableListOf<Boolean>();
        for (email in emails) {
            results.add(email.matches(".*@.*\\..*"));
        }
        return results;
    }
}
