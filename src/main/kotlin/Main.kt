import com.beust.klaxon.JsonArray
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser.Companion.default
import facts.RelationLoader
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.io.path.absolutePathString
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis
import parsing.EngineConfiguration
import parsing.Sorting
import parsing.parseEngineConfigurations
import proofs.addAllProofs
import tests.Test
import tests.testgeneration.addAllTests

fun main() {
    val time = measureTimeMillis { executeConfigurations() }

    println()
    println("Done in ${time / 1000} seconds")
}

class ResultContext(
    val engine: String,
    val version: String,
    val results: ConcurrentLinkedQueue<TestResult>,
)

private fun executeConfigurations() {
    val engines = parseEngineConfigurations()
    val allTests = generateTests()
    println("Found ${allTests.size} tests")

    for (engine in engines) {
        val sortedTests = sortTests(engine, allTests)
        if (engine.testsSavePath != null) saveTests(engine.name, engine.testsSavePath, sortedTests)

        if (engine.enabled) executeTests(engine, sortedTests)
    }
}

private fun executeTests(engine: EngineConfiguration, tests: Collection<Test>) {
    val engineResults = ResultContext(engine.name, engine.version, ConcurrentLinkedQueue())
    val numTests = tests.size
    val progress = AtomicInteger(0)
    val failedTests = AtomicInteger(0)
    println()
    println("Running $numTests tests on engine \"${engine.name}\"")

    val executor = Executor(engine)

    val t = printProgressbar(progress, failedTests, numTests)

    val time = measureTimeMillis {
        tests.parallelStream().forEach {
            val testResult = executor.runTest(it, engine.deadline ?: 30)

            engineResults.results.add(testResult)

            progress.getAndAdd(1)

            if (testResult.result != testResult.expected) {
                if (engine.verbose == true) {
                    print("\r") // Replace the progress bar
                    printTestResult(testResult)
                }
                failedTests.getAndAdd(1)
            }
        }
        engine.terminate()
    }

    t.join()

    var passed = 0
    var failed = 0
    var unknown = 0

    engineResults.results.forEach {
        when (it.result) {
            it.expected -> passed++
            else -> if (it.result == ResultType.EXCEPTION) unknown++ else failed++
        }
    }

    println()
    println(
        "${passed}/$numTests tests succeeded (${(passed * 100.0 / numTests).roundToInt()}%)" +
            (if (unknown == 0) "" else " ($unknown failed due to exceptions)") +
            " in ${time / 1000} seconds")
    saveResults(engineResults)
}

private fun generateTests(): Collection<Test> {
    val allRelations = ProofSearcher().addAllProofs().findNewRelations(RelationLoader.relations)
    // ProofLimiter(3).limit(allRelations)
    return TestGenerator().addAllTests().generateTests(allRelations)
}

private fun sortTests(engine: EngineConfiguration, tests: Collection<Test>): Collection<Test> {
    val operators = listOf("||", "\\\\", "&&", "consistency:", "refinement:")
    var out = ArrayList(tests)

    if (engine.queryComplexity != null) { // Query Complexity
        val (lower, upper) = engine.bounds()

        out =
            ArrayList(
                out.filter { x ->
                    x.queries().all { y -> y.occurrences(operators) in lower..upper }
                })
    }

    if (engine.testCount != null) { // Count
        out =
            when (engine.testSorting) {
                Sorting.FILO -> ArrayList(out.takeLast(engine.testCount))
                Sorting.FIFO -> ArrayList(out.take(engine.testCount))
                Sorting.RoundRobin -> getEqualTests(out, engine.testCount)
                Sorting.Random,
                null -> ArrayList(out.shuffled().take(engine.testCount))
            }
    }
    return out
}

private fun String.occurrences(strings: Collection<String>): Int {
    var count = 0
    for (string in strings) {
        count += this.occurrences(string)
    }
    return count
}

private fun String.occurrences(string: String): Int =
    this.windowed(string.length).count { it == string }

private fun getEqualTests(tests: Collection<Test>, count: Int): ArrayList<Test> {
    val map: HashMap<Pair<String, String>, ArrayList<Test>> = HashMap()
    tests.forEach { x -> map.getOrPut(Pair(x.type, x.testSuite)) { ArrayList() }.add(x) }

    val out = ArrayList<Test>()
    if (map.keys.size == 0) return out
    for (test in map.values.toList()) {
        out.addAll(test.take(count / map.keys.size))
    }
    return out
}

private fun saveTests(engineName: String, path: String, tests: Collection<Test>) {
    val file = File(Paths.get(path).absolutePathString()).normalize()
    if (!file.canWrite() && file.exists()) throw Exception("Cannot write queries to file $path")
    println("Saving the tests for $engineName in $file")
    val writer = file.writer(Charset.defaultCharset())
    val initStr =
        "Tests generated for engine '$engineName' on ${SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())}"
    val sep = System.lineSeparator()
    writer.write(initStr + sep)
    writer.write("${"-".repeat(initStr.length)}\n\n")

    // Partitions by the different systems
    // The first partition is written to the file
    // The second is assigned to `remainingTests`
    // This is done until no more tests can be found in `remainingTests`
    var remainingTests = tests
    while (!remainingTests.isEmpty()) {
        val first = remainingTests.first() // Partitions based on the first elements project
        val (writeTests, otherTests) =
            remainingTests.partition { it.projectPath == first.projectPath }

        writer.write(first.projectPath + sep)
        for (test in writeTests) writer.write(test.format("\t"))

        writer.write(sep)
        remainingTests = otherTests
    }
    writer.close()
}

private fun printProgressbar(progress: AtomicInteger, failed: AtomicInteger, max: Int): Thread {
    return thread(start = true, isDaemon = true) {
        val anim = "|/-\\"
        do {
            val p = progress.get()
            val x = p * 100 / max

            val f = failed.get()

            val data =
                "\r${anim[x % anim.length]} $x% [$p/$max]" +
                    if (f > 0) {
                        " $ANSI_RED$f tests failed$ANSI_RESET"
                    } else {
                        ""
                    }
            print(data)
            Thread.sleep(100)
        } while (p != max)
    }
}

private fun saveResults(results: ResultContext) {
    var path = "results/${results.engine}/${results.version}"
    val dir = File(path)
    dir.mkdirs()
    // dir.lastModified()
    var fileNumber = dir.listFiles()!!.size
    while (File("$path/$fileNumber.json").exists()) {
        fileNumber++
    }
    path += "/$fileNumber.json"
    println("Saving results for engine '${results.engine}' in $path")

    writeJsonToFile(path, results.results.map { it.result })
}

private fun writeJsonToFile(filePath: String, results: Any) {
    val json = toPrettyJsonString(results)
    writeToNewFile(filePath, json)
}

private fun toPrettyJsonString(results: Any): String {
    val builder = StringBuilder(Klaxon().toJsonString(results))
    return (default().parse(builder) as JsonArray<*>).toJsonString(true)
}

private fun writeToNewFile(filePath: String, text: String) {
    val file = File(filePath)
    file.createNewFile()
    file.writeText(text)
}
