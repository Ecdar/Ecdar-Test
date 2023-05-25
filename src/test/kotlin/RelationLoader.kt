import facts.RelationLoader
import org.junit.jupiter.api.Test

class RelationLoader {
    @Test
    fun universityActions() {
        val expected = hashSetOf("pub?", "patent!", "grant?", "coin!")
        val actual = RelationLoader.actionMap["Uni"]!!["Adm2"]!!
        println("expected: $expected")
        println("actual: $actual")
        assert(expected == actual)
    }

    @Test
    fun delayRefinementActionsZ1() {
        assert(hashSetOf("o!", "go!") == RelationLoader.actionMap["Delay"]!!["Z1"]!!)
    }

    @Test
    fun delayRefinementActionsZ2() {
        assert(hashSetOf("o!") == RelationLoader.actionMap["Delay"]!!["Z2"]!!)
    }

    @Test
    fun delayRefinementActionsZ3() {
        assert(hashSetOf("o!") == RelationLoader.actionMap["Delay"]!!["Z3"]!!)
    }

    @Test
    fun delayRefinementActionsZ4() {
        assert(hashSetOf("o!") == RelationLoader.actionMap["Delay"]!!["Z4"]!!)
    }

    @Test
    fun delayRefinementActionsZ5() {
        assert(hashSetOf("i?") == RelationLoader.actionMap["Delay"]!!["Z5"]!!)
    }

    @Test
    fun delayRefinementActionsZ6() {
        assert(hashSetOf("i!") == RelationLoader.actionMap["Delay"]!!["Z6"]!!)
    }

    @Test
    fun delayRefinementActionsZ7() {
        assert(hashSetOf("i?") == RelationLoader.actionMap["Delay"]!!["Z7"]!!)
    }
}
