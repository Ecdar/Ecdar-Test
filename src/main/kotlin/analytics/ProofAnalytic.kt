package analytics

import parsing.System
import proofs.Proof

interface ProofAnalytic {

    fun recordStart(components: ArrayList<System>) {}

    fun recordEnd(components: ArrayList<System>) {}

    fun recordIteration(dirtyComponents: HashSet<System>, components: ArrayList<System>) {}

    fun recordProofIteration(
        dirtyComponents: HashSet<System>,
        components: ArrayList<System>,
        proof: Proof
    ) {}
}
