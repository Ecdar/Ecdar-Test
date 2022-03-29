import com.beust.klaxon.*
import java.lang.Exception

data class Result (
    val engine: String,
    val exception: String?,
    val result: ResultType,
    val expected: ResultType,
    val test: JsonObject,
    val time: Int
)

data class TestType (
    val projectPath: String,
    val query: String,
    val testSuite: String,
    val type: String)

fun main() {
    val results = parseResults("last_run.json")

    var numTests = results.size
    var failed = 0
    var errored = 0
    var succeded = 0

    results.forEach {
        val result = ResultType.valueOf(it["result"] as String)
        val expected = ResultType.valueOf(it["expected"] as String)

        when(result){
            expected -> succeded++
            ResultType.EXCEPTION -> errored++
            else -> failed++
        }
    }

    println("$succeded/$numTests tests succeded")

    if (succeded == numTests) return

    println("$failed tests failed")
    println("$errored failed due to expections")
    println()

    results.forEach {
        val result = ResultType.valueOf(it["result"] as String)
        val expected = ResultType.valueOf(it["expected"] as String)

        if (result != expected) {

            val test = (it["test"]!!) as JsonObject
            val testSuite = test["testSuite"]
            val projectPath = test["projectPath"]
            val query = getQuery(test)

            //val testName = "${it["engine"]}::${testSuite}::${query}"
            val testName = query

            println("Expected ${expected.colored()}, but was ${result.colored()} in $testName")
            println("Rerun with arguments: \"$query\" -i $projectPath \n")

        }

    }
}

fun parseResults(file : String): List<JsonObject> {
    return Klaxon().parser().parse(file) as List<JsonObject>
}

fun getQuery(test: JsonObject) : String {
    val query = test["query"]
    if (query == null) {
        val tests = test["tests"] as JsonArray<JsonObject>
        var result = ""
        for (t in tests) {
            result += "${getQuery(t)}; "
        }
        return "$result"
    }
    return "$query"
}
