package proofs

import parsing.Composition
import parsing.Quotient
import parsing.System

class QuotientRule : Proof() {
    override val maxContribution: Int = 50
    //Quotient rule: S || T ≤ X => S ≤ X \ T
    override fun search(component: System, ctx: ProofSearcher.IterationContext) {
        val SCT = component // S || T
        if (SCT is Composition) {
            val composition = SCT.children
            if (composition.size<2) return
            for (T in composition) {
                val children = composition.toHashSet()
                children.remove(T)

                val S = if (children.size==1) {
                    ctx.addNewComponent(children.first())
                } else {
                    ctx.addNewComponent(Composition(children))
                }

                for (X in SCT.thisRefines) {
                    val XQT = ctx.addNewComponent(Quotient(X, T)) // X \ T

                    if (S.refines(XQT)) {
                        ctx.setDirty(XQT, this)
                        ctx.setDirty(S, this)
                    }
                }
            }
        }
    }
}