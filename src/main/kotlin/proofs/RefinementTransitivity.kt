package proofs

import ProofSearcher
import parsing.System

class RefinementTransitivity : Proof() {
    override fun search(component: System, ctx: ProofSearcher.IterationContext) {
        for (lhs in component.refinesThis) {
            for (rhs in component.thisRefines) {
                addTransitiveRefinement(lhs, rhs, ctx)
            }
        }
    }

    private fun addTransitiveRefinement(lhs: System, rhs: System, ctx: ProofSearcher.IterationContext) {
        if (lhs.refines(rhs)) {
            ctx.setDirty(lhs, this)
            ctx.setDirty(rhs, this)
        }
    }
}