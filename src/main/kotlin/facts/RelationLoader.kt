package facts

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import parsing.RelationParserFacade
import parsing.RelationVisitor
import parsing.System

object RelationLoader {
    private val visitor = RelationVisitor()
    val relations: ArrayList<System>

    val prefixMap =
        mapOf(
            "AG" to "samples/json/AG",
            "Uni" to "samples/json/EcdarUniversity",
            "Conjunction" to "samples/json/Conjunction",
            "Conjun" to "samples/xml/conjun.xml",
            "BigRefinement" to "samples/json/BigRefinement",
            "DelayAdd" to "samples/json/DelayAdd",
            "Unspec" to "samples/json/Unspec",
            "Loop" to "samples/xml/loop.xml",
            "Delay" to "samples/xml/delayRefinement.xml",
            "Extrapolation" to "samples/xml/extrapolation_test.xml",
            "Misc" to "samples/xml/misc_test.xml",
            "Cons" to "samples/xml/ConsTests.xml")

    val actionMap = HashMap<String, Map<String, Set<String>>>()

    init {
        parseActions()

        val parse = RelationParserFacade.parse(File("relations.txt").readText(Charsets.UTF_8))!!
        parse.accept(visitor)
        relations = visitor.components
    }

    fun getInputs(prefix: String, name: String): HashSet<String> {
        return actionMap[prefix]!![name]!!
            .filter { it.endsWith("?") && !it.startsWith("*") }
            .map { it.dropLast(1) }
            .toHashSet()
    }

    fun getOutputs(prefix: String, name: String): HashSet<String> {
        return actionMap[prefix]!![name]!!
            .filter { it.endsWith("!") && !it.startsWith("*") }
            .map { it.dropLast(1) }
            .toHashSet()
    }

    private fun parseActions() {
        for ((prefix, path) in prefixMap.entries) {
            val file = File(path)
            if (file.isDirectory) {
                // JSON
                val map = HashMap<String, Set<String>>()

                val components = File(path, "Components")
                val files = components.listFiles()!!
                for (comp in files) {
                    val obj = Klaxon().parser().parse(comp.path.toString()) as JsonObject
                    val name = obj.string("name")!!
                    val edges = obj.array<JsonObject>("edges")!!
                    val actions = HashSet<String>()
                    for (edge in edges) {
                        val isInput = edge.string("status")!! == "INPUT"
                        val sync = edge.string("sync")!!
                        actions.add("$sync${if (isInput) "?" else "!"}")
                    }
                    map[name] = actions
                }
                actionMap[prefix] = map
            } else {
                // XML
                val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                val doc = builder.parse(file).documentElement

                val children = doc.childNodes
                val map = HashMap<String, Set<String>>()
                for (i in 0 until children.length) {
                    val child = children.item(i)

                    if (child.nodeName == "template") {
                        val templateChildren = child.childNodes
                        var name = "Unknown"
                        val actions = HashSet<String>()
                        for (j in 0 until templateChildren.length) {
                            val node = templateChildren.item(j)
                            if (node.nodeName == "name") {
                                name = node.textContent
                            } else if (node.nodeName == "transition") {
                                val transitionChildren = node.childNodes
                                for (k in 0 until transitionChildren.length) {
                                    val label = transitionChildren.item(k)
                                    if (label.nodeName == "label" &&
                                        label.attributes.getNamedItem("kind").nodeValue ==
                                            "synchronisation") {
                                        actions.add(label.textContent)
                                    }
                                }
                            }
                        }

                        map[name] = actions
                    }
                }
                actionMap[prefix] = map
            }
        }
    }
}
