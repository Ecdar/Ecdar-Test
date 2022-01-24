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
import kotlin.system.measureTimeMillis

fun main() {
    val time = measureTimeMillis {
    val tests = generateTests()

    val results = executeTests(tests)

    writeJsonToFile("last_run.json", results)
    }

    println()
    println("Done in ${time / 1000} seconds")
}

private fun generateTests(): Collection<Test> {
    val allRelations = ProofSearcher().addAllProofs().findNewRelations(RelationLoader.relations)
    return TestGenerator().addAllTests().generateTests(allRelations)
}

private fun executeTests(tests: Collection<Test>): Iterable<TestResult> {
    val engines = parseEngineConfigurations()
    val fullResults = ArrayList<TestResult>()

    for (engine in engines) {
        val results = ConcurrentLinkedQueue<TestResult>()
        val numTests = tests.size
        val progress = AtomicInteger(0)
        println()
        println("Running $numTests tests on engine \"${engine.name}\"")

        val executor = Executor(engine)

        val t = printProgressbar(progress, numTests)

        val time = measureTimeMillis {
            tests.parallelStream().forEach {
                val start = System.currentTimeMillis()
                val result = try {
                    executor.runTest(it)
                } catch (e: Throwable) {
                    val r = TestResult(it, null)
                    r.exception = e.message
                    r
                }

                result.time = System.currentTimeMillis() - start
                result.engine = engine.name

                results.add(result)

                progress.getAndAdd(1)
            }
            engine.terminate()
        }

        t.join()

        var passed = 0
        var failed = 0
        var unknown = 0

        results.forEach { when(it.result) {
            true ->  passed++
            false -> failed++
            null -> unknown++
            }
        }

        println()
        println("${numTests-passed}/$numTests tests failed (${(numTests-passed)*100/numTests}%)" +
                (if (unknown == 0) "" else " ($unknown due to exceptions)") +
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



