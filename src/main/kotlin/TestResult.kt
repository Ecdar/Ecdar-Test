import tests.Test

class TestResult(
    val test: Test,
    val result: Boolean?
) {
    var time: Long? = null
    var engine: String? = null
    var exception: String? = null
}

