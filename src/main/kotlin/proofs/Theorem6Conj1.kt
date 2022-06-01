package proofs

import ProofSearcher
import parsing.Component
import parsing.Conjunction
import parsing.System

class Theorem6Conj1 : Proof() {
    //HSCC.Theorem6.part1, locally consistent S and T : S ∧ T ≤ S and S ∧ T ≤ T (∧ is the same as &&)
    override fun search(component: System, ctx: ProofSearcher.IterationContext) {
        //TODO: we cant currently apply this to compositions because the ConsistentCompositions adds too many
        if (component !is Component) return

        if (component.isKnownLocallyConsistent()) {
            for (other in ctx.dirtyComponents) {
                if (other !is Component || component == other)
                    continue

                if (other.isKnownLocallyConsistent() && component.sharesAlphabet(other)) {
                    addNewConjunction(ctx, component, other)
                }
            }
        }
    }

    private fun addNewConjunction(ctx: ProofSearcher.IterationContext, component: System, other: System) {
        val conj = ctx.addNewComponent(Conjunction(component, other))
        conj?.let{
            setParentRefinesChildren(conj, ctx)
        }
    }

    private fun setParentRefinesChildren(
        parent: System,
        ctx: ProofSearcher.IterationContext
    ) {
        for (child in parent.children) {
            if (parent.refines(child)) {
                ctx.setDirty(child, this)
            }
        }
    }
}