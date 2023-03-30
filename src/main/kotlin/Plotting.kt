import Graphing.createDensityPlot
import Graphing.createLinePlot
import Graphing.open
import java.io.File

fun main() {
    plotEngineMeans("line", logScale = true)
    //plotEngineMeans("density", logScale = true)
    //plotVersionMeans("Reveaal", "density", logScale = true)
    //plotVersionMeans("Reveaal", "line", logScale = true)
}

fun plotEngineMeans(type: String = "density", logScale: Boolean = false) {
    val file = File("results")
    file.mkdirs()
    val data = file.list().associateWith { getEngineMean(it) }
    plot(data, type, logScale)
}


fun plotVersionMeans(engine: String, type: String = "density", logScale: Boolean = false) {
    val data = File("results/$engine").list().associate { "$engine:$it" to getVersionMean("$engine/$it") }
    plot(data, type, logScale)
}

fun plot(data: Map<String, List<TestResult>>, type: String, logScale: Boolean) {
    var name = data.keys.joinToString("-") { it.replace(":", "_") }
    if (logScale) {
        name += "_log"
    }
    when (type) {
        "density" -> createDensityPlot(data, logScale).open(name + "_density")
        "line" -> createLinePlot(data, logScale).open(name + "_line")
    }
}

fun getEngineMean(engine: String) : List<TestResult> {
    val path = "results/$engine"
    return combine(File(path).list().map { getVersionMean("$engine/$it") })
}

fun getVersionMean(version: String) : List<TestResult> {
    val path = "results/$version"
    return combine(File(path).list().map { getFile("$version/$it") })
}

fun getFile(file: String) :List<TestResult> {
    val path = "results/$file"
    //return parseResults(path).filter { it.result == it.expected }
    return parseResults(path).flatMap { it.inner.ifEmpty { listOf(it) } }.filter {it.result != ResultType.EXCEPTION}
}

fun combine(results: List<List<TestResult>>) : List<TestResult> {
    data class TestCounter (val test: TestResult, var count: Int)
    var combined: MutableList<TestCounter> = mutableListOf()
    results.forEach { outer -> if(combined.isEmpty()) {combined = outer.map {TestCounter(it, 1)}.toMutableList()} else {outer.forEach {
            inner ->
        val other = combined.find { it.test.test.query == inner.test.query && it.test.result == inner.result }
        if (other == null) {
            combined.add(TestCounter(inner, 1))
        } else {
            other.test.time = other.test.time?.plus(inner.time!!)
            other.count += 1
        }
    }}}

    return combined.map {
        val test = it.test
        test.time = test.time!!/it.count
        test }
}
