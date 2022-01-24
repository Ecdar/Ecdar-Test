package proofs

import ProofSearcher
import parsing.System

class SelfRefinement : Proof() {
    override fun search(component: System, ctx: ProofSearcher.IterationContext) {
        if (component.isKnownLocallyConsistent()) {
            makeSelfRefining(component, ctx)
        }
    }

    private fun makeSelfRefining(component: System, ctx: ProofSearcher.IterationContext) {
        if (component.refines(component)) {
            ctx.setDirty(component, this)
        }
    }
}