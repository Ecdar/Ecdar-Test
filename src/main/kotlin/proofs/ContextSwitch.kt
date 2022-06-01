package proofs

import ProofSearcher
import parsing.Composition
import parsing.Conjunction
import parsing.Quotient
import parsing.System

class ContextSwitch : Proof() {
    override val maxContribution: Int = 4000
    // A <= B and B <= A imply C[A] == C[B] (A can be replaced by B in any context)
    override fun search(component: System, ctx: ProofSearcher.IterationContext) {
        val equal = component.thisRefines.intersect(component.refinesThis)

        if (equal.size > 1) {
            for (other in equal) {
                if (other.sameAs(component)) continue

                val changed = other.thisRefines.addAll(component.thisRefines) or
                        other.thisNotRefines.addAll(component.thisNotRefines) or
                        other.refinesThis.addAll(component.refinesThis) or
                        other.notRefinesThis.addAll(component.notRefinesThis)

                if (changed) ctx.setDirty(other, this)

                for (parent in component.parents.toList()) {
                    replace(component, other, parent, ctx)
                }

            }
        }
    }


    private fun replace(oldChild: System, newChild: System, parent: System, ctx: ProofSearcher.IterationContext) {
        assert(parent is Conjunction || parent is Composition || parent is Quotient)

        val children = parent.children.toHashSet()
        children.remove(oldChild)
        children.add(newChild)

        if (children.size < 2) return

        val parentClone: System

        when (parent) {
            is Quotient -> {
                val S = if (parent.S == oldChild) {
                    newChild
                } else {
                    parent.S
                }
                val T = if (parent.T == oldChild) {
                    newChild
                } else {
                    parent.T
                }

                parentClone = Quotient(S, T)
            }
            is Conjunction -> {
                parentClone = Conjunction(children)
            }
            is Composition -> {
                parentClone = Composition(children)
            }
            else -> {
                return
            }
        }


        val newParent = ctx.addNewComponent(parentClone)
        newParent?.let {
            val changed = newParent.refinesThis.addAll(parent.refinesThis) or
                    newParent.thisRefines.addAll(parent.thisRefines) or
                    newParent.notRefinesThis.addAll(parent.notRefinesThis) or
                    newParent.thisNotRefines.addAll(parent.thisNotRefines)

            if (changed) ctx.setDirty(newParent, this)

            if (parent.thisRefines.add(newParent) or parent.refinesThis.add(newParent)) ctx.setDirty(parent, this)
        }

        /* //This is done in the next iteration instead
            for (parpar in parent.parents) {
                replace(parent, newParent, parpar, ctx)
            }
        */

    }
}


