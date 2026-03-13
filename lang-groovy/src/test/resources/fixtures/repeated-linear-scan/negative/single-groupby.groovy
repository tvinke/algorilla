class ReportService {
    void generateReport(List transactions) {
        def grouped = transactions.groupBy { it.category }
    }
}
