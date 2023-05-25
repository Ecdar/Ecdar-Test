package analytics

import parsing.System

class RefinementsAnalysis : ProofAnalytic {
    var startingRefinements = 0
    var endingRefinements = 0
    var addedRefinements = 0

    private var lastRefinements = 0
    var addedPerIteration = ArrayList<Int>()

    override fun recordStart(components: ArrayList<System>) {
        startingRefinements = refinementCount(components)
        lastRefinements = startingRefinements
    }

    override fun recordEnd(components: ArrayList<System>) {
        endingRefinements = refinementCount(components)
        addedRefinements = endingRefinements - startingRefinements
    }

    override fun recordIteration(dirtyComponents: HashSet<System>, components: ArrayList<System>) {
        val next = refinementCount(components)
        addedPerIteration.add(next - lastRefinements)
        lastRefinements = next
    }

    fun printFindings() {
        println("Refinement proof searching resulted in")
        println(
            "$addedRefinements new refinement relations going from $startingRefinements to $endingRefinements")
        println("Iteration breakdown")
        var iterationCount = 1
        for (added in addedPerIteration) {
            println("Iteration $iterationCount: $added")
            iterationCount++
        }
    }

    private fun refinementCount(components: ArrayList<System>): Int {
        var count = 0
        for (comp in components) {
            count += comp.thisRefines.count()
        }

        return count
    }
}
