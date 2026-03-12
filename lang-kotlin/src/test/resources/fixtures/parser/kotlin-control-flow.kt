class ControlFlowExamples {
    fun withForIn(items: List<String>) {
        for (item in items) {
            println(item)
        }
    }

    fun withWhile(items: MutableList<Int>) {
        while (items.isNotEmpty()) {
            items.removeFirst()
        }
    }

    fun withDoWhile() {
        do {
            process()
        } while (hasMore())
    }

    fun withIfElse(x: Int): String {
        if (x > 0) {
            return "positive"
        } else {
            return "non-positive"
        }
    }

    fun withWhen(x: Int): String {
        return when (x) {
            1 -> "one"
            2 -> "two"
            else -> "other"
        }
    }

    fun withTryCatch() {
        try {
            doSomething()
        } catch (e: Exception) {
            handleError(e)
        } finally {
            cleanup()
        }
    }
}
