import facts.RelationLoader
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import parsing.RelationParserFacade

internal class RelationParserFacadeTest {

    @Test
    fun parse1() {
        val result = RelationParserFacade.parse("A.A || A.B </= B.B")
        assertTrue(result?.exception == null)
    }

    @Test
    fun parse2() {
        val result = RelationParserFacade.parse("A.A || A.B <= B.B \n ")
        assertTrue(result?.exception == null)
        result?.facts()?.fact()
    }

    @Test
    fun parse3() {
        val result = RelationParserFacade.parse("A.A || A.B <= B.B \n A.A || A.B <= B.B ")

        assertTrue(result?.exception == null)
    }

    @Test
    fun visit1() {

        for (comp in RelationLoader.relations) {
            val refines =
                comp.thisRefines
                    .toArray()
                    .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.toString() })
                    .toString()
            val oRefines =
                comp.refinesThis
                    .toArray()
                    .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.toString() })
                    .toString()

            println("$comp refines $refines and $oRefines refine it")
        }
    }
}
