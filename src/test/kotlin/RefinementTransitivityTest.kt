import analytics.RefinementsAnalysis
import facts.RelationLoader
import org.junit.jupiter.api.Test
import proofs.addRefinementProofs

internal class RefinementTransitivityTest {

    @Test
    fun visit1() {

        val refinementAnalytics = RefinementsAnalysis()
        val searcher = ProofSearcher().addRefinementProofs().addAnalytic(refinementAnalytics)
        searcher.findNewRelations(RelationLoader.relations)

        refinementAnalytics.printFindings()
    }

    @Test
    fun refinementTransitivityTest1() {
        val proofSearcher = ProofSearcher().addRefinementProofs()

        val factSheet =
            """
            AG.A <= AG.G
            AG.G <= AG.AA
        """
                .trimIndent()

        val expectedSheet =
            """
            AG.A <= AG.AA
        """
                .trimIndent()

        assert(proofSearchContains(factSheet, expectedSheet, proofSearcher))
    }

    @Test
    fun refinementTransitivityTest2() {
        val proofSearcher = ProofSearcher().addRefinementProofs()

        val factSheet =
            """
            AG.A <= AG.G
            AG.G <= AG.AA
            AG.AA <= AG.Imp
        """
                .trimIndent()

        val expectedSheet =
            """
            AG.A <= AG.Imp
        """
                .trimIndent()

        assert(proofSearchContains(factSheet, expectedSheet, proofSearcher))
    }

    @Test
    fun refinementTransitivityTest3() {
        val proofSearcher = ProofSearcher().addRefinementProofs()

        val factSheet =
            """
            AG.A <= AG.G
            AG.AA <= AG.AA
        """
                .trimIndent()

        val expectedSheet =
            """
            AG.A <= AG.AA
        """
                .trimIndent()

        assert(!proofSearchContains(factSheet, expectedSheet, proofSearcher))
    }

    @Test
    fun refinementTransitivityCircularTest() {
        val proofSearcher = ProofSearcher().addRefinementProofs()

        val factSheet =
            """
            AG.A <= AG.G
            AG.G <= AG.AA
            AG.AA <= AG.A
        """
                .trimIndent()

        val expectedSheet =
            """
            AG.A <= AG.AA
            AG.G <= AG.A
            AG.AA <= AG.G
        """
                .trimIndent()

        assert(proofSearchContains(factSheet, expectedSheet, proofSearcher))
    }
}
