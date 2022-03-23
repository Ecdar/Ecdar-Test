package proofs

import ProofSearcher

fun ProofSearcher.addAllProofs(): ProofSearcher {
    return this.addRefinementProofs()
        .addConsistencyProofs()
        .addProof(ContextSwitch()) //Incredibly slow generation
}

fun ProofSearcher.addRefinementProofs(): ProofSearcher {
    return this.addProof(RefinementTransitivity())
        .addProof(SelfRefinement())
        .addProof(Theorem6Conj1())
        .addProof(Theorem6Conj2())
        .addProof(QuotientRule())
}

fun ProofSearcher.addQuotientProofs() : ProofSearcher {
    return this.addProof(QuotientRule())
}

fun ProofSearcher.addConsistencyProofs(): ProofSearcher {
    return this.addProof(ConsistentRefinements())
        .addProof(ConsistentCompositions())
}