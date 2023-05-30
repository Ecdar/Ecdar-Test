class QueryResultReader(fullText: String) {
    private val lines: Iterator<String> = fullText.lines().iterator()

    init {
        skipToResults()
    }

    fun nextResult(): QueryResult {
        if (!hasNext()) throw Exception("No more results")

        return readNextResult()
    }

    fun hasNext(): Boolean {
        return lines.hasNext()
    }

    private fun readNextResult(): QueryResult {
        val line = lines.next()
        val split = line.split(" -- ")
        return QueryResult(split[0], split[1])
    }

    private fun skipToResults() {
        while (lines.hasNext()) {
            if (lines.next() == "Query results:") return
        }

        throw Exception("No query results found, model checker likely failed")
    }
}
