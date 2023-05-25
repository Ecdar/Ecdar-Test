package proofs

import ProofSearcher
import parsing.System

class RefinementTransitivity : Proof() {
    override fun search(component: System, ctx: ProofSearcher.IterationContext) {
        for (lhs in component.refinesThis) {
            if (component.inputs.containsAll(lhs.inputs) &&
                component.outputs ==
                    lhs.outputs) { // S1 inputs ⊆ S2 inputs and S1 outputs = S2 outputs
                for (rhs in component.thisRefines) {
                    if (rhs.outputs.containsAll(component.outputs) &&
                        rhs.inputs ==
                            component.inputs) { // S2 inputs = S3 inputs and S2 outputs ⊇ S3 outputs
                        addTransitiveRefinement(lhs, rhs, ctx)
                    }
                }
            }
        }
    }

    private fun addTransitiveRefinement(
        lhs: System,
        rhs: System,
        ctx: ProofSearcher.IterationContext
    ) {
        if (lhs.refines(rhs)) {
            ctx.setDirty(lhs, this)
            ctx.setDirty(rhs, this)
        }
    }
}
