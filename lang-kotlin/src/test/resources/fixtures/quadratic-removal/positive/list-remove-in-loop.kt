class Processor {
    fun removeOdds(numbers: MutableList<Int>) {
        for (n in numbers) {
            numbers.remove(n);
        }
    }
}
