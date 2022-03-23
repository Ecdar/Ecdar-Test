import parsing.System

class ProofLimiter(val maxComplexity: Int) {
    fun limit(components: ArrayList<System>) {
        val toRemove = components.filter { it.components > maxComplexity }
        toRemove.forEach {
            for (other in it.refinesThis.union(it.thisRefines.union(it.thisNotRefines.union(it.notRefinesThis)))) {
                other.refinesThis.remove(it)
                other.thisRefines.remove(it)
                other.thisNotRefines.remove(it)
                other.notRefinesThis.remove(it)
            }
        }
        components.removeAll(toRemove.toSet())
    }
}