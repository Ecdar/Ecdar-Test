import com.beust.klaxon.JsonArray
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser.Companion.default
import facts.RelationLoader
import parsing.parseEngineConfigurations
import proofs.addAllProofs
import tests.Test
import tests.testgeneration.addAllTests
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime

fun main() {
    val time = measureTimeMillis {
    val tests = generateTests()

    val results = executeTests(tests)

    writeJsonToFile("last_run.json", results)
    }

    println("Done in ${time/1000} seconds")
}

private fun generateTests(): Collection<Test> {
    val allRelations = ProofSearcher().addAllProofs().findNewRelations(RelationLoader.relations)
    return TestGenerator().addAllTests().generateTests(allRelations)
}

private fun executeTests(tests: Collection<Test>): Iterable<TestResult> {
    val engines = parseEngineConfigurations()
    val results = ConcurrentLinkedQueue<TestResult>()

    for (engine in engines) {
        val executor = Executor(engine)
        tests.parallelStream().forEach {
            results.add(executor.runTest(it))
        }
        engine.terminate()
    }
    return results
}

private fun writeJsonToFile(filePath: String, results: Any) {
    val json = toPrettyJsonString(results)
    writeToNewFile(filePath, json)
}

private fun toPrettyJsonString(results: Any): String {
    val builder = StringBuilder(Klaxon().toJsonString(results))
    val prettyJson = (default(  ).parse(builder) as JsonArray<*>).toJsonString(true)
    return prettyJson
}

private fun writeToNewFile(filePath: String, text: String) {
    val file = File(filePath)
    file.createNewFile()
    file.writeText(text)
}



