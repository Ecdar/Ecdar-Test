import com.beust.klaxon.*
import java.io.File
import java.nio.file.Files
import kotlin.io.path.extension
import kotlin.io.path.getLastModifiedTime
import kotlin.math.roundToInt

const val MAX_RESULTS_PRINTED: Int = 20

fun main() {
    printLatestResults()
    //printLatestResults("results/J-ecdar/UCDD")
    //printLatestResults("results/Reveaal")
    //printResults("results/Reveaal/Main/0.json")
}

fun printLatestResults(folder: String = "results") {
    val dir = File(folder)
    if (!dir.exists() || !dir.isDirectory) {
        println("\"$folder\" is not a directory")
        return
    }

    val sorted = Files.walk(dir.toPath())
        .filter(Files::isRegularFile)
        .filter {it.extension == "json"}
        .sorted { o1, o2 -> o2.getLastModifiedTime().compareTo(o1.getLastModifiedTime())}
    val file = sorted.findFirst()
    if (file.isEmpty) {
        println("Could not find any .json results in \"$folder\"")
        return
    }
    printResults(file.get().toString())
}


fun printResults(file: String) {
    val sdf = java.text.SimpleDateFormat("HH:mm:ss' 'yyyy-MM-dd")
    val date = java.util.Date(File(file).lastModified())

    println("Printing results for file \"$file\"\nFile last modified ${sdf.format(date)}\n")
    val results = parseResults(file)

    val numTests = results.size
    var failed = 0
    var errored = 0
    var succeded = 0
    val succedingResults = ResultType.values().associateWith { 0 }.toMutableMap()
    val failedResults = ResultType.values().associateWith { 0 }.toMutableMap()

    results.forEach {
        when(it.result){
            it.expected -> {succeded++; succedingResults[it.expected] = succedingResults[it.expected]!! + 1 }
            ResultType.EXCEPTION -> errored++
            else -> {failed++; failedResults[it.expected] = failedResults[it.expected]!! + 1 }
        }
    }

    println("$succeded/$numTests tests succeeded (${(succeded*100.0/numTests).roundToInt()}%)")

    ResultType.values().forEach {
        val succ = succedingResults[it]!!
        val all = succedingResults[it]!! + failedResults[it]!!
        val text = "${succ}/${all}"
        if (all != 0)
            println("$text of ${it.colored()} tests succeeded")
    }



    println("$failed tests failed")
    println("$errored failed due to exceptions")
    println()

    val times = ArrayList<Double>()
    var prints = 0

    // Operators to check for usage in failed queries
    val operators = listOf("||", "\\\\", "&&", "consistency:", "refinement:")
    val operatorFailedCounts = operators.associateWith { 0 }.toMutableMap()
    val operatorExceptionCounts = operators.associateWith { 0 }.toMutableMap()
    val operatorSucceededCounts = operators.associateWith { 0 }.toMutableMap()

    results.forEach {
        if (it.result != it.expected) {
            if (prints<MAX_RESULTS_PRINTED) { // Print at most maxPrints times
                println()
                printTestResult(it)
            }

            for (key in operators) {
                if(it.test.query.contains(key)) {
                    // If it was an exception we check which sub-query caused it.
                    if (it.result == ResultType.EXCEPTION) {
                        if (it.inner.isEmpty() || it.inner.any { it.result == ResultType.EXCEPTION && it.test.query.contains(key) }) {
                            operatorExceptionCounts[key] = operatorExceptionCounts[key]!! + 1
                        }
                    } else {
                        operatorFailedCounts[key] = operatorFailedCounts[key]!! + 1
                    }
                }
            }

            prints ++
        } else {
            for (key in operators) {
                if(it.test.query.contains(key)) {
                    operatorSucceededCounts[key] = operatorSucceededCounts[key]!! + 1
                }
            }
            it.time?.let { times.add(it) }
        }
    }

    if (prints > MAX_RESULTS_PRINTED) {
        println("Printed the first $MAX_RESULTS_PRINTED of $prints failed tests...\n")
    }

    println("$succeded/$numTests tests succeeded (${(succeded*100.0/numTests).roundToInt()}%)\n")
    if (succeded != numTests) {
        fun Map<String, Int>.toPercentages(max:Int): Collection<String>  {
            if(max <= 0) return listOf()
            return this.filterValues { it != 0 }.map { (key, value) -> "\"$key\" used in ${(value*100.0/max).roundToInt()}%" }
        }

        val usedFailedOperators = operatorFailedCounts.toPercentages(failed)
        if (usedFailedOperators.isNotEmpty()) {
            println("Operator ${usedFailedOperators.joinToString(", ")} of $failed failing queries")
        }

        val usedExceptionOperators = operatorExceptionCounts.toPercentages(errored)
        if (usedExceptionOperators.isNotEmpty()) {
            println("Operator ${usedExceptionOperators.joinToString(", ")} of $errored queries causing exceptions")
        }

        val usedSucceededOperators = operatorSucceededCounts.toPercentages(succeded)
        if (usedSucceededOperators.isNotEmpty()) {
            println("Operator ${usedSucceededOperators.joinToString(", ")} of $succeded succeeded queries")
        }
    }
    if (times.size > 0) {
    println("\nMedian runtime of successful queries: ${median(times)} ms")
    }
}

fun printTestResult(result: TestResult) {
    val testName = result.test.query
    println("Expected ${result.expected.colored()}, but was ${result.result.colored()} in $testName")
    if (result.result == ResultType.EXCEPTION) {
        println("${ResultType.EXCEPTION.colored()}: ${result.exception}")
    }

    println("Rerun with arguments: \"${getInnerQueries(result).joinToString("; ")}\" -i ${result.test.projectPath} \n")
}

fun getInnerQueries(result: TestResult) : List<String> {
    return if (result.inner.isEmpty()) {
        listOf(result.test.query)
    } else {
        result.inner.flatMap { getInnerQueries(it) }
    }
}

fun median(l: List<Double>) = l.sorted().let { (it[it.size / 2] + it[(it.size - 1) / 2]) / 2.0 }

fun parseResults(file : String): List<TestResult> {
    return (Klaxon().parseArray<TestResult>(File(file)))!!
}
