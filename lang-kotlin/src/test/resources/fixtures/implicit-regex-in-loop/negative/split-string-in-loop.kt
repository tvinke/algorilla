class CsvParser {
    fun parseCsvLines(lines: List<String>): List<List<String>> {
        val result = mutableListOf<List<String>>();
        for (line in lines) {
            result.add(line.split(","));
        }
        return result;
    }
}
