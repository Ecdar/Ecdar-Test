import com.beust.klaxon.JsonArray
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser.Companion.default
import com.google.type.DateTime
import facts.RelationLoader
import parsing.EngineConfiguration
import parsing.Sorting
import parsing.parseEngineConfigurations
import proofs.addAllProofs
import tests.Test
import tests.testgeneration.addAllTests
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

fun main() {
    val time = measureTimeMillis {
        val results = executeTests()
        saveResults(results.toList())
    }

    println()
    println("Done in ${time / 1000} seconds")
}

class ResultContext(val result: TestResult,
                    val engine: String,
                    val version: String)

private fun executeTests(): Iterable<ResultContext> {
    val engines = parseEngineConfigurations()
    val fullResults = ArrayList<ResultContext>()
    val allTests = generateTests()
    println("Found ${allTests.size} tests")

    for (engine in engines) {
        val tests = sortTests(engine, allTests)
        if (engine.omitTests == true) { //Omitting tests and/or saving them
            val path = engine.testsSavePath ?: "./${engine.name}_tests" //TODO : Default file
            saveTests(engine.name, path, tests)
            println("Executing the tests for ${engine.name} have been omitted")
            println()
            continue
        } else if (engine.testsSavePath != null)
            saveTests(engine.name, engine.testsSavePath, tests)

        val results = ConcurrentLinkedQueue<ResultContext>()
        val numTests = tests.size
        val progress = AtomicInteger(0)
        val failedTests = AtomicInteger(0)
        println()
        println("Running $numTests tests on engine \"${engine.name}\"")

        val executor = Executor(engine)

        val t = printProgressbar(progress, failedTests, numTests)

        val time = measureTimeMillis {
            tests.parallelStream().forEach {

                val result = executor.runTest(it, engine.deadline)

                results.add(ResultContext(result, engine.name, engine.version))

                progress.getAndAdd(1)

                if (result.result != result.expected) {
                    if (engine.verbose == true) {
                        print("\r") // Replace the progress bar
                        printTestResult(result)
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

        results.forEach { when(it.result.result) {
            it.result.expected ->  passed++
            else -> if (it.result.result == ResultType.EXCEPTION) unknown++ else failed++
            }
        }

        println()
        println("${passed}/$numTests tests succeeded (${(passed*100.0/numTests).roundToInt()}%)" +
                (if (unknown == 0) "" else " ($unknown failed due to exceptions)") +
                " in ${time/1000} seconds")


        fullResults.addAll(results)
    }
    return fullResults
}

private fun generateTests(): Collection<Test> {
    val allRelations = ProofSearcher().addAllProofs().findNewRelations(RelationLoader.relations)
    //ProofLimiter(3).limit(allRelations)
    return TestGenerator().addAllTests().generateTests(allRelations)
}


val OPERATORS = listOf("||", "\\\\", "&&", "consistency:", "refinement:")
private fun sortTests(engine: EngineConfiguration, tests: Collection<Test>) : Collection<Test> {
    var out = ArrayList(tests)

    if (engine.queryComplexity != null) { //Query Complexity
        val upper: Int; val lower: Int
        if (engine.queryComplexity.size >= 2) {
            upper = engine.queryComplexity[1]
            lower = engine.queryComplexity.first()
        } else {
            upper = engine.queryComplexity.firstOrNull() ?: Int.MAX_VALUE
            lower = 0
        }

        if (upper < lower)
            throw Exception("The upper bound for `queryComplexity` can't be less than the lower bound")

        out = ArrayList(out.filter { x ->
            x.queries().all { y ->
                var c = 0
                for (op in OPERATORS) {
                    c += y.split(op)
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray().size - 1
                }
                c in lower..upper
            }
        })
    }

    if (engine.testCount != null) {  //Count
        out = when (engine.testSorting) {
            Sorting.FILO -> ArrayList(out.takeLast(engine.testCount))
            Sorting.FIFO -> ArrayList(out.take(engine.testCount))
            Sorting.RoundRobin -> getEqualTests(out, engine.testCount)
            Sorting.Random, null -> ArrayList(out.shuffled().take(engine.testCount))
        }
    }
    return out
}

private fun getEqualTests(tests: Collection<Test>, count: Int): ArrayList<Test> {
    val map: HashMap<Pair<String, String>, ArrayList<Test>> = HashMap()
    tests.forEach { x -> map.getOrPut(Pair(x.type, x.testSuite)) { ArrayList() }.add(x) }
    println(map.keys)
    val out = ArrayList<Test>()
    for (test in map.values.toList()) {
        out.addAll(test.take(count / map.keys.size))
    }
    return out
}

private fun saveTests(engineName: String, path: String, tests: Collection<Test>) {
    val file = File(Paths.get(path).absolutePathString()).normalize()
    println("Saving the tests for $engineName in $file")
    val writer = file.writer(Charset.defaultCharset())
    val initStr = "Tests generated for engine '$engineName' on ${SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())}"
    writer.write(initStr + "\n")
    writer.write("${"-" * initStr.length}\n\n")
    var testCol = tests
    while (!testCol.isEmpty()) {
        val first = testCol.first()
        val pair = testCol.partition { it.projectPath == first.projectPath }

        writer.write(first.projectPath + "\n")
        for (test in pair.first) {
            writer.write(formatTest(test, "\t"))
        }
        writer.write("\n")
        testCol = pair.second
    }
    writer.close()
}

private fun formatTest(test: Test, indenter: String): String {
    var out = "$indenter${test.type}: ${test.testSuite}\n"
    for (query in test.queries()) {
        out += "$indenter$indenter\"${query}\"\n"
    }
    return out
}

private operator fun CharSequence.times(count: Int): String {
    return repeat(count)
}

private fun printProgressbar(progress: AtomicInteger, failed: AtomicInteger, max: Int) : Thread {
    return thread(start = true, isDaemon = true) {
        val anim = "|/-\\"
        do  {
            val p = progress.get()
            val x = p*100 / max

            val f = failed.get()

            val data = "\r${anim[x % anim.length]} $x% [$p/$max]" + if (f>0) {" $ANSI_RED$f tests failed$ANSI_RESET"} else {""}
            print(data)
            Thread.sleep(100)
        } while(p != max)
    }
}

private fun saveResults(results: List<ResultContext>) {
    for ((engine, tests) in results.groupBy { it.engine }) {
        for ((version, versionResults) in tests.groupBy { it.version }) {
            val path = "results/$engine/$version"
            val dir = File(path)
            dir.mkdirs()
            //dir.lastModified()
            var fileNumber = dir.listFiles()!!.size
            while (File("$path/$fileNumber.json").exists()) {
                fileNumber++
            }

            writeJsonToFile("$path/$fileNumber.json", versionResults.map { it.result })
        }
    }
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



