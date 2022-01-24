package proofs

import ProofSearcher.IterationContext
import parsing.System

abstract class Proof {
    var contribution: Int = 0

    open val maxContribution: Int = Int.MAX_VALUE

    fun reset() {
        contribution = 0
    }

    fun doSearch(component: System, ctx: IterationContext) {
        if (contribution < maxContribution) {
            search(component, ctx)

            contribution += ctx.proofContributions.getOrDefault(this, 0)
        }
    }

    abstract fun search(component: System, ctx: IterationContext)
}