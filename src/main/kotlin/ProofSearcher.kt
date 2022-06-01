import analytics.ProofAnalytic
import parsing.System
import proofs.Proof

class ProofSearcher {
    private val theorems = ArrayList<Proof>()
    private val analytics = ArrayList<ProofAnalytic>()

    fun addProof(proof: Proof): ProofSearcher {
        if (!theorems.contains(proof)) {
            theorems.add(proof)
        }

        return this
    }

    fun addAnalytic(analytic: ProofAnalytic): ProofSearcher {
        analytics.add(analytic)
        return this
    }

    fun findNewRelations(components: ArrayList<System>): ArrayList<System> {
        analytics.forEach { analytic -> analytic.recordStart(components) }

        val result = runSearch(components)

        analytics.forEach { analytic -> analytic.recordEnd(components) }

        return result
    }

    private fun runSearch(components: ArrayList<System>): ArrayList<System> {
        val allComponents = ArrayList<System>(components)
        var dirtyComponents = HashSet(components)

        var iteration = 0

        theorems.forEach { it.reset() }

        while (dirtyComponents.isNotEmpty()) {
            println("Dirty components/components: ${dirtyComponents.count()}/${allComponents.count()} it: $iteration")

            val iterationContext = IterationContext(allComponents, dirtyComponents, iteration)
            dirtyComponents = searchIteration(iterationContext)

            analytics.forEach { analytic -> analytic.recordIteration(dirtyComponents, allComponents) }
            iteration++
        }

        println("${allComponents.size} components identified")
        theorems.forEach { println("Proof ${it.javaClass.simpleName} added ${it.contribution} relations") }

        return allComponents
    }

    private fun searchIteration(
        iterationContext: IterationContext
    ): HashSet<System> {

        for (theorem in theorems) {
            for (comp in iterationContext.dirtyComponents) {
                theorem.doSearch(comp, iterationContext)
            }
            analytics.forEach { analytic ->
                analytic.recordProofIteration(
                    iterationContext.dirtyComponents,
                    iterationContext.components,
                    theorem
                )
            }
        }

        return iterationContext.newlyMarkedComponents
    }

    class IterationContext(
        val components: ArrayList<System>,
        val dirtyComponents: HashSet<System>,
        val currentIteration: Int
    ) {
        var newlyMarkedComponents = HashSet<System>()
        var contribution = 0

        fun setDirty(component: System, source: Proof) {
            newlyMarkedComponents.add(component)
            contribution += 1
        }

        fun addNewComponent(component: System): System? {
            if (!component.isValid()) {
                return null
            }

            for (comp in components) {
                if (comp.sameAs(component)) {
                    return comp
                }
            }

            for (child in component.children) {
                child.parents.add(component)
                newlyMarkedComponents.add(child)
            }

            components.add(component)
            newlyMarkedComponents.add(component)
            return component
        }
    }

}