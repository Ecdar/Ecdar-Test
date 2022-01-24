package proofs

import ProofSearcher
import parsing.Conjunction
import parsing.System

class Theorem6Conj2 : Proof() {
    //HSCC.Theorem6.part2, locally consistent S, U and T sharing the same alphabet: (U <= S) and (U <= T) implies U <= (S && T)
    override fun search(component: System, ctx: ProofSearcher.IterationContext) {
        if (component.thisRefines.size > 1) {
            val refines = HashSet<System>()
            //Add the components who share alphabet
            refines.addAll(component.thisRefines.filter { it.sharesAlphabet(component) })
            for (lhs in refines) {
                for (rhs in refines) {
                    if (!lhs.sameAs(rhs) && rhs.getProjectFolder() == lhs.getProjectFolder()) {
                        var newComp: System = Conjunction(lhs, rhs)
                        newComp = ctx.addNewComponent(newComp)

                        if (component.refines(newComp)) {
                            ctx.setDirty(newComp, this)
                            ctx.setDirty(component, this)
                        }
                    }
                }
            }

        }

    }
}
