package proofs

import parsing.Composition
import parsing.Quotient
import parsing.System

class QuotientRule : Proof() {
    override val maxContribution: Int = 1000
    //Quotient rule: S || T ≤ X iff? S ≤ X \\ T
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

                S?.let{

                for (X in SCT.thisRefines) {
                    val XQT = ctx.addNewComponent(Quotient(X, T)) // X \\ T
                    XQT?.let{
                    if (S.refines(XQT)) {
                        ctx.setDirty(XQT, this)
                        ctx.setDirty(S, this)
                    }}
                }

                for (X in SCT.thisNotRefines) {
                    val XQT = ctx.addNewComponent(Quotient(X, T)) // X \\ T
                    XQT?.let{
                    if (S.notRefines(XQT)) {
                        ctx.setDirty(XQT, this)
                        ctx.setDirty(S, this)
                    }}
                }
                }
            }
        }
    }
}