import analytics.ComponentsAdded
import facts.RelationLoader
import kotlin.reflect.typeOf
import org.junit.jupiter.api.Test
import parsing.*
import proofs.ConsistentCompositions
import proofs.addAllProofs
import proofs.addConsistencyProofs

internal class ConsistencyTests {
    @Test
    fun consistencyComposition() {
        val proofSearcher = ProofSearcher().addConsistencyProofs()

        val factSheet =
            """
            locally-consistent: AG.A
            locally-consistent: AG.G
        """
                .trimIndent()

        val expectedSheet =
            """
            locally-consistent: AG.A || AG.G
        """
                .trimIndent()

        assert(proofSearchContains(factSheet, expectedSheet, proofSearcher))
    }

    @ExperimentalStdlibApi
    @Test
    fun visit1() {
        val compsAddedAnalysis = ComponentsAdded(arrayListOf(typeOf<ConsistentCompositions>()))
        val searcher = ProofSearcher().addAllProofs().addAnalytic(compsAddedAnalysis)

        searcher.findNewRelations(RelationLoader.relations)

        compsAddedAnalysis.printFindings()
    }

    fun consistencyCount(components: ArrayList<System>): Int {
        var count = 0
        for (comp in components) {
            if (comp.isLocallyConsistent.orElse(false)) {
                count++
            }
        }

        return count
    }

    fun nonConsistencyCount(components: ArrayList<System>): Int {
        var count = 0
        for (comp in components) {
            if (!comp.isLocallyConsistent.orElse(true)) {
                count++
            }
        }

        return count
    }
}
