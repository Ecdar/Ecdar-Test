package parsing

import facts.RelationLoader.getInputs
import facts.RelationLoader.getOutputs
import facts.RelationLoader.prefixMap
import java.util.*

interface System {

    var children: HashSet<System>
    var refinesThis: HashSet<System>
    var thisRefines: HashSet<System>
    var notRefinesThis: HashSet<System>
    var thisNotRefines: HashSet<System>
    var parents: HashSet<System>
    val depth: Int
    var isLocallyConsistent: Optional<Boolean>
    var inputs: HashSet<String>
    var outputs: HashSet<String>

    fun isKnownLocallyConsistent(): Boolean {
        return isLocallyConsistent.orElse(false)
    }

    fun isKnownNotLocallyConsistent(): Boolean {
        return !isLocallyConsistent.orElse(true)
    }

    fun sharesAlphabet(other: System): Boolean {
        //Actions with the same name are assumed different if in another project
        return getProjectFolder() == other.getProjectFolder() &&
                inputs == other.inputs &&
                outputs == other.outputs
    }

    fun sameAs(other: System): Boolean

    fun refines(spec: System): Boolean {
        return thisRefines.add(spec) or
                spec.refinesThis.add(this)
    }

    fun notRefines(spec: System): Boolean {
        return thisNotRefines.add(spec) or spec.notRefinesThis.add(this)
    }

    override fun toString(): String
    fun getProjectFolder(): String

    fun getName(): String
}

class Component(val prefix: String, val comp: String) : System {
    override var inputs = getInputs(prefix, comp)
    override var outputs = getOutputs(prefix, comp)

    init {
        assert(prefix in prefixMap) { "Unknown prefix $prefix for component $comp" }
    }

    override var children = HashSet<System>()
    override var refinesThis = HashSet<System>()
    override var thisRefines = HashSet<System>()
    override var notRefinesThis = HashSet<System>()
    override var thisNotRefines = HashSet<System>()
    override var parents = HashSet<System>()
    override val depth: Int
        get() = 0
    override var isLocallyConsistent: Optional<Boolean> = Optional.empty()


    override fun sameAs(other: System): Boolean {
        if (other is Component) {
            return prefix == other.prefix && comp == other.comp
        }
        return false
    }

    override fun getProjectFolder(): String {
        return prefixMap[prefix]!!
    }

    override fun getName(): String {
        return comp
    }

    override fun toString(): String {
        return "$prefix.$comp"
    }

}


class Quotient : System {
    constructor(T: System, S: System) {
        assert(T.getProjectFolder() == S.getProjectFolder())
        children.add(T)
        children.add(S)
        this.T = T
        this.S = S
        setActions()
    }

    private fun setActions() {
        inputs = T.inputs.union(S.outputs).toHashSet()
        outputs = T.outputs.intersect(S.outputs).toHashSet()
    }

    var T : System
    var S : System

    override var inputs = HashSet<String>()
    override var outputs = HashSet<String>()
    override var children = HashSet<System>()
    override var refinesThis = HashSet<System>()
    override var thisRefines = HashSet<System>()
    override var notRefinesThis = HashSet<System>()
    override var thisNotRefines = HashSet<System>()
    override var parents = HashSet<System>()
    override val depth: Int
        get() = 1 + (this.children.map { it.depth }.maxOrNull() ?: 0)
    override var isLocallyConsistent: Optional<Boolean> = Optional.empty()


    override fun sameAs(other: System): Boolean {
        if (other is Quotient) {
            return S == other.S && T == other.T
        }
        return false
    }

    override fun getProjectFolder(): String {
        val it = children.iterator()
        val folderPath = it.next().getProjectFolder()
        for (system in it) {
            if (folderPath != system.getProjectFolder()) {
                throw Exception("Children have different projects folders")
            }
        }
        return folderPath
    }

    override fun getName(): String {
        return "(${T.getName()} // ${S.getName()})"
    }

    override fun toString(): String {
        return "($T // $S)"
    }
}

class Conjunction : System {
    constructor(left: System, right: System) {
        assert(left.getProjectFolder() == right.getProjectFolder())

        if (left is Conjunction) {
            children.addAll(left.children)
        } else {
            children.add(left)
        }
        if (right is Conjunction) {
            children.addAll(right.children)
        } else {
            children.add(right)
        }
        assert(children.size > 1)

        setActions()
    }

    constructor(children: HashSet<System>) {
        val folder = children.elementAt(0).getProjectFolder()
        assert(children.all { c -> c.getProjectFolder() == folder })


        assert(children.size > 1)
        for (child in children) {
            if (child is Conjunction) {
                this.children.addAll(child.children)
            } else {
                this.children.add(child)
            }
        }

        setActions()
    }

    private fun setActions() {
        var first = true
        for (child in children) {
            if (first) {
                inputs.addAll(child.inputs)
                outputs.addAll(child.outputs)
                first = false
                continue
            }

            inputs = inputs.intersect(child.inputs).toHashSet()
            outputs = outputs.intersect(child.outputs).toHashSet()
        }
    }


    override var inputs = HashSet<String>()
    override var outputs = HashSet<String>()
    override var children = HashSet<System>()
    override var refinesThis = HashSet<System>()
    override var thisRefines = HashSet<System>()
    override var notRefinesThis = HashSet<System>()
    override var thisNotRefines = HashSet<System>()
    override var parents = HashSet<System>()
    override val depth: Int
        get() = 1 + (this.children.map { it.depth }.maxOrNull() ?: 0)
    override var isLocallyConsistent: Optional<Boolean> = Optional.empty()


    override fun sameAs(other: System): Boolean {
        if (other is Conjunction) {
            return children == other.children
        }
        return false
    }

    override fun getProjectFolder(): String {
        val it = children.iterator()
        val folderPath = it.next().getProjectFolder()
        for (system in it) {
            if (folderPath != system.getProjectFolder()) {
                throw Exception("Children have different projects folders")
            }
        }
        return folderPath
    }

    override fun getName(): String {
        return "(${
            children.toArray().map { child -> (child as System).getName() }.sortedWith(
                compareBy(String.CASE_INSENSITIVE_ORDER) { it }
            ).joinToString(" && ")
        })"
    }

    override fun toString(): String {
        return "(${
            children.toArray().sortedWith(
                compareBy(String.CASE_INSENSITIVE_ORDER) { it.toString() }
            ).joinToString(" && ")
        })"
    }


}

class Composition : System {
    constructor(left: System, right: System) {
        assert(left.getProjectFolder() == right.getProjectFolder())
        if (left is Composition) {
            children.addAll(left.children)
        } else {
            children.add(left)
        }
        if (right is Composition) {
            children.addAll(right.children)
        } else {
            children.add(right)
        }
        assert(children.size > 1)

        setActions()
    }

    constructor(children: HashSet<System>) {
        assert(children.size > 1)
        val folder = children.elementAt(0).getProjectFolder()
        assert(children.all { c -> c.getProjectFolder() == folder })
        for (child in children) {
            if (child is Composition) {
                this.children.addAll(child.children)
            } else {
                this.children.add(child)
            }
        }

        setActions()
    }

    private fun setActions() {
        for (child in children) {
            outputs.addAll(child.outputs)

            val potentialInputs = child.outputs.toHashSet()
            for (other in children) {
                if (other == child) continue
                potentialInputs.removeAll(other.outputs)
            }
        }
    }

    override var inputs = HashSet<String>()
    override var outputs = HashSet<String>()
    override var children = HashSet<System>()
    override var refinesThis = HashSet<System>()
    override var thisRefines = HashSet<System>()
    override var notRefinesThis = HashSet<System>()
    override var thisNotRefines = HashSet<System>()
    override var parents = HashSet<System>()
    override val depth: Int
        get() = 1 + (this.children.map { it.depth }.maxOrNull() ?: 0)
    override var isLocallyConsistent: Optional<Boolean> = Optional.empty()


    override fun sameAs(other: System): Boolean {
        if (other is Composition) {
            return children == other.children
        }
        return false
    }

    override fun getProjectFolder(): String {
        val it = children.iterator()
        val folderPath = it.next().getProjectFolder()
        for (system in it) {
            if (folderPath != system.getProjectFolder()) {
                throw Exception("Children have different projects folders")
            }
        }
        return folderPath
    }

    override fun getName(): String {
        return "(${
            children.toArray().map { child -> (child as System).getName() }.sortedWith(
                compareBy(String.CASE_INSENSITIVE_ORDER) { it }
            ).joinToString(" || ")
        })"
    }

    override fun toString(): String {
        return "(${
            children.toArray().sortedWith(
                compareBy(String.CASE_INSENSITIVE_ORDER) { it.toString() }
            ).joinToString(" || ")
        })"
    }

}