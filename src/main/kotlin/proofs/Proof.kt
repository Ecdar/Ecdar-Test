package proofs

import ProofSearcher.IterationContext
import parsing.System

abstract class Proof {
    var contribution: Int = 0

    open val maxContribution: Int = 20000

    fun reset() {
        contribution = 0
    }

    fun doSearch(component: System, ctx: IterationContext) {
        if (contribution < maxContribution) {
            search(component, ctx)

            contribution += ctx.contribution
            ctx.contribution = 0
        }
    }

    abstract fun search(component: System, ctx: IterationContext)
}