package proofs

import parsing.Composition
import parsing.Quotient
import parsing.System

class QuotientRule : Proof() {
    override val maxContribution: Int = 1000
    //Quotient rule: S || T ≤ X iff? S ≤ X \\ T
    override fun search(component: System, ctx: ProofSearcher.IterationContext) {
        val S_comp_T = component // S || T
        if (S_comp_T is Composition) {
            val composition = S_comp_T.children
            if (composition.size<2) return
            for (T in composition) {
                val children = composition.toHashSet()
                children.remove(T)

                val S = if (children.size==1) {
                    ctx.addNewComponent(children.first())
                } else {
                    ctx.addNewComponent(Composition(children))
                }

                S?.let{

                    for (X in S_comp_T.thisRefines) {
                        val X_quotient_T = ctx.addNewComponent(Quotient(X, T)) // X \\ T
                        X_quotient_T?.let{

                        if (S.refines(X_quotient_T)) {
                            ctx.setDirty(X_quotient_T, this)
                            ctx.setDirty(S, this)
                        }}
                    }

                    for (X in S_comp_T.thisNotRefines) {
                        val X_quotient_T = ctx.addNewComponent(Quotient(X, T)) // X \\ T
                        X_quotient_T?.let{
                        if (S.notRefines(X_quotient_T)) {
                            ctx.setDirty(X_quotient_T, this)
                            ctx.setDirty(S, this)
                        }}
                    }
                }
            }
        }
    }
}