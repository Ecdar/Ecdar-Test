import tests.Test

class TestResult(
    val test: Test,
    val result: Boolean
) {
    val timestamp = System.currentTimeMillis() / 1000
    var engine: String? = null
}

