import jetbrains.letsPlot.geom.geomDensity
import jetbrains.letsPlot.intern.Plot
import java.awt.Desktop
import java.io.File
import jetbrains.datalore.plot.PlotHtmlExport
import jetbrains.datalore.plot.PlotHtmlHelper.scriptUrl
import jetbrains.letsPlot.*
import jetbrains.letsPlot.export.VersionChecker
import jetbrains.letsPlot.geom.geomLine
import jetbrains.letsPlot.intern.Feature
import jetbrains.letsPlot.intern.toSpec
import jetbrains.letsPlot.label.labs
import jetbrains.letsPlot.scale.scaleXLog10
import jetbrains.letsPlot.scale.scaleYLog10
import jetbrains.letsPlot.tooltips.layerTooltips
import java.lang.Math.log
import kotlin.math.ln


object Graphing {
    fun createPlot(input:  Map<String, List<TestResult>>, layer: Feature): Plot {
        var common = input.iterator().next().value.map { Pair(it.test.query, it.result) }.toSet()
        input.forEach {common = common.intersect(it.value.map { Pair(it.test.query, it.result) }.toSet())}

        val commonInput = input.map { it.key to it.value.filter { common.contains(Pair(it.test.query, it.result)) } }.toMap()

        val maxTime = mutableMapOf<String, Double>()
        val series = input.size

        //commonInput.forEach { it.value.forEach { maxTime[it.test.query] = it.time!!/series + maxTime.getOrDefault(it.test.query, 0.0)  } }
        commonInput.forEach { it.value.forEach { maxTime[it.test.query] = (it.time!!) /series + maxTime.getOrDefault(it.test.query, 0.0)  } }

        val value = commonInput.iterator().next().value
        println("Common queries: ${value.size}")
        println("Common satisfied ${value.filter {it.result == ResultType.SATISFIED}.size}")
        println("Common unsatisfied ${value.filter {it.result == ResultType.UNSATISFIED}.size}")

        val data = mapOf (
            "time" to commonInput.flatMap { it.value.map { it.time!! } },
            "version" to commonInput.flatMap { it.value.map { _ -> it.key } },
            "query" to commonInput.flatMap { it.value.map { it.test.query }  },
            "queryOrder" to commonInput.flatMap { it.value.map { maxTime[it.test.query]!! } },
            "queryResult" to commonInput.flatMap { it.value.map { it.result } }
        )

        return letsPlot(data) + layer
    }

    fun createLinePlot(input:  Map<String, List<TestResult>>, logScale: Boolean) : Plot {
        var layers = geomLine(alpha = 0.8, size= 1.0, tooltips = layerTooltips().line("@query").line("@time").line("@queryResult").anchor("top_left")) { x= asDiscrete("query", orderBy = "queryOrder", order = 1, label="Queries ordered by runtime"); y = "time"; color = "version"; } +
                ggsize(2000, 800) +
                theme(axisTextX = elementBlank()) +
                labs(y = "Runtime (ms)", title = "Line Plot")
        if (logScale) {
            layers += scaleYLog10()
        }
        return createPlot(input, layers)
    }

    fun createDensityPlot(input:  Map<String, List<TestResult>>, logScale: Boolean) : Plot {
       var layers = geomDensity(alpha = .5) { x = "time"; color = "version"; fill = "version"; } +
               ggsize(1500, 500) +
               labs(x = "Runtime (ms)", y = "Density", title = "Density Plot")

        if (logScale) {
            layers += scaleXLog10()
        }
        return createPlot(input, layers)
    }

    fun openInBrowser(content: String, name: String) {
        val dir = File(System.getProperty("user.dir"), "plots")
        dir.mkdir()
        val file = File(dir.canonicalPath, "$name.html")
        file.createNewFile()
        file.writeText(content)

        Desktop.getDesktop().browse(file.toURI())
    }

    fun Plot.open(name: String) = openPlot(this, name)

    private fun openPlot(plot: Plot, name: String) {
        // Export to HTML.
        // Note: if all you need is to save HTML to a file than you can just use the 'ggsave()' function.
        val content = PlotHtmlExport.buildHtmlFromRawSpecs(plot.toSpec(), scriptUrl(VersionChecker.letsPlotJsVersion), false)
        openInBrowser(content, name)
    }
}
