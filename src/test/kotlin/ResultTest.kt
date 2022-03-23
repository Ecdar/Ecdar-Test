import com.beust.klaxon.*
import facts.RelationLoader
import kotlinx.coroutines.yield
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.function.Executable
import parsing.EngineConfiguration
import tests.Test
import java.io.File

data class Result (
    val engine: String,
    val exception: String?,
    val result: Boolean?,
    val test: JsonObject,
    val time: Int
)

data class TestType (
    val projectPath: String,
    val query: String,
    val testSuite: String,
    val type: String)

class ResultTest {



    fun parseResults(): List<JsonObject> {
        return Klaxon().parser().parse("last_run.json") as List<JsonObject>
    }

    @TestFactory
    fun main(): Iterable<DynamicTest?> = sequence {

        parseResults().forEach {
            val test = (it["test"]!!) as JsonObject
            val testSuite = test["testSuite"]
            val projectPath = test["projectPath"]
            val query = getQuery(test)
            val result = it["result"]

            val testName = "${it["engine"]}::${testSuite}::${projectPath}::${query}"
            yield(DynamicTest.dynamicTest(testName) {
                println(testName)
                assertEquals(true, result) {"Exception: ${it["exception"]}"} })
        }

    }.toList()

    fun getQuery(test: JsonObject) : String {
        val query = test["query"]
        if (query == null) {
            val tests = test["tests"] as JsonArray<JsonObject>
            var result = "{"
            for (t in tests) {
                result += "${getQuery(t)}; "
            }
            return "$result}"
        }
        return "$query"
    }
}