package proofs

import ProofSearcher.IterationContext
import parsing.System

abstract class Proof {
    var contribution: Int = 0
    open val maxContribution: Int = 20000
    abstract val kind: ProofKind

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

    fun markComp(component: System) {
        component.relatedProofs = component.relatedProofs or kind.value
    }
    abstract fun search(component: System, ctx: IterationContext)
}

enum class ProofKind(val value: Int) {
    ConsistentCompositions(1 shl 0),
    ConsistentRefinements(1 shl 1),
    ContextSwitch(1 shl 2),
    QuotientRule(1 shl 3),
    RefinementTransitivity(1 shl 4),
    SelfRefinement(1 shl 5),
    Theorem6Conj1(1 shl 6),
    Theorem6Conj2(1 shl 7)
}