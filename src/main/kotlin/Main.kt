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
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis

fun main() {
    val time = measureTimeMillis {
        val tests = generateTests()
        println("Found ${tests.size} tests")
        //return@measureTimeMillis

        val results = executeTests(tests)

        saveResults(results.toList())
    }

    println()
    println("Done in ${time / 1000} seconds")
}

private fun generateTests(): Collection<Test> {
    val allRelations = ProofSearcher().addAllProofs().findNewRelations(RelationLoader.relations)
    //ProofLimiter(3).limit(allRelations)
    return TestGenerator().addAllTests().generateTests(allRelations)
}

class ResultContext(val result: TestResult,
                    val engine: String,
                    val version: String)

private fun executeTests(tests: Collection<Test>): Iterable<ResultContext> {
    val engines = parseEngineConfigurations()
    val fullResults = ArrayList<ResultContext>()

    for (engine in engines) {
        val results = ConcurrentLinkedQueue<ResultContext>()
        val numTests = tests.size
        val progress = AtomicInteger(0)
        println()
        println("Running $numTests tests on engine \"${engine.name}\"")

        val executor = Executor(engine)

        val t = printProgressbar(progress, numTests)

        val time = measureTimeMillis {
            tests.parallelStream().forEach {

                val result = executor.runTest(it)

                results.add(ResultContext(result, engine.name, engine.version))

                progress.getAndAdd(1)
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

private fun printProgressbar(progress: AtomicInteger, max: Int) : Thread {
    return thread(start = true, isDaemon = true) {
        val anim = "|/-\\"
        do  {
            val p = progress.get()
            val x = p*100 / max

            val data = "\r" + anim[x % anim.length] + " $x% [$p/$max]"
            System.out.write(data.toByteArray())
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
    val prettyJson = (default(  ).parse(builder) as JsonArray<*>).toJsonString(true)
    return prettyJson
}

private fun writeToNewFile(filePath: String, text: String) {
    val file = File(filePath)
    file.createNewFile()
    file.writeText(text)
}



