package proofs

import ProofSearcher
import parsing.Composition
import parsing.System
import java.util.*

class ConsistentCompositions : Proof() {
    override fun search(component: System, ctx: ProofSearcher.IterationContext) {
        if (component.isLocallyConsistent.isEmpty && component is Composition) {
            calculateConsistency(component, ctx)
        } else if (component.isKnownLocallyConsistent() && component is Composition) {
            makeChildrenConsistent(component, ctx)
        }
    }

    private fun calculateConsistency(component: System, ctx: ProofSearcher.IterationContext) {
        if (allConsistent(component.children)) {
            setConsistency(component, ctx, true)
        } else if (anyNonConsistent(component.children)) {
            setConsistency(component, ctx, false)
        }
    }

    private fun makeChildrenConsistent(component: System, ctx: ProofSearcher.IterationContext) {
        for (child in component.children) {
            if (child.isLocallyConsistent.isEmpty) {
                setConsistency(child, ctx, true)
            }
        }
    }

    private fun setConsistency(component: System, ctx: ProofSearcher.IterationContext, consistency: Boolean) {
        component.isLocallyConsistent = Optional.of(consistency)
        ctx.setDirty(component, this)
    }

    private fun allConsistent(systems: Iterable<System>) =
        systems.all { c -> c.isKnownLocallyConsistent() }

    private fun anyNonConsistent(systems: Iterable<System>) =
        systems.any { c -> c.isKnownNotLocallyConsistent() }
}