package proofs

import ProofSearcher
import java.util.*
import parsing.System

class ConsistentRefinements : Proof() {
    override fun search(component: System, ctx: ProofSearcher.IterationContext) {
        if (hasAnyRefinementRelations(component)) {
            makeSystemConsistent(component, ctx)
        }
    }

    private fun makeSystemConsistent(component: System, ctx: ProofSearcher.IterationContext) {
        if (component.isLocallyConsistent.isEmpty && ctx.currentIteration == 0) {
            component.isLocallyConsistent = Optional.of(true)
            ctx.setDirty(component, this)
        }
    }

    private fun hasAnyRefinementRelations(component: System) =
        component.refinesThis.isNotEmpty() || component.thisRefines.isNotEmpty()
}
