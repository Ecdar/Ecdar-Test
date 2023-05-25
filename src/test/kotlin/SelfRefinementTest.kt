import org.junit.jupiter.api.Test
import proofs.ConsistentRefinements
import proofs.SelfRefinement

class SelfRefinementTest {
    @Test
    fun selfRefinementTest1() {
        val proofSearcher = ProofSearcher().addProof(SelfRefinement())

        val factSheet =
            """
            locally-consistent: AG.A
        """
                .trimIndent()

        val expectedSheet =
            """
            AG.A <= AG.A
        """
                .trimIndent()

        assert(proofSearchContains(factSheet, expectedSheet, proofSearcher))
    }

    @Test
    fun selfRefinementTest2() {
        val proofSearcher = ProofSearcher().addProof(SelfRefinement())

        val factSheet =
            """
            AG.A <= AG.G
        """
                .trimIndent()

        val expectedSheet =
            """
            AG.A <= AG.A
            AG.G <= AG.G
        """
                .trimIndent()

        assert(!proofSearchContains(factSheet, expectedSheet, proofSearcher))
    }

    @Test
    fun selfRefinementTest3() {
        val proofSearcher =
            ProofSearcher().addProof(SelfRefinement()).addProof(ConsistentRefinements())

        val factSheet =
            """
            AG.A <= AG.G
        """
                .trimIndent()

        val expectedSheet =
            """
            AG.A <= AG.A
            AG.G <= AG.G
        """
                .trimIndent()

        assert(proofSearchContains(factSheet, expectedSheet, proofSearcher))
    }
}
