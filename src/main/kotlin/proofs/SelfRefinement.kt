package proofs

import ProofSearcher
import parsing.System

class SelfRefinement : Proof() {
    override fun search(component: System, ctx: ProofSearcher.IterationContext) {
        if (hasAnyRefinementRelations(
            component)) { // E.g. the component is assumed to be consistent
            makeSelfRefining(component, ctx)
        }
    }

    private fun makeSelfRefining(component: System, ctx: ProofSearcher.IterationContext) {
        if (component.refines(component)) {
            ctx.setDirty(component, this)
        }
    }

    private fun hasAnyRefinementRelations(component: System) =
        component.refinesThis.isNotEmpty() || component.thisRefines.isNotEmpty()
}
