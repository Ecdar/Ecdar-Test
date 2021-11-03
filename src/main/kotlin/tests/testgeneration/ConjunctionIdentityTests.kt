package tests.testgeneration

import TestGenerator
import parsing.Conjunction
import parsing.System
import tests.ImplicationTest
import tests.SatisfiedTest
import tests.Test

fun TestGenerator.addConjunctionIdentityTests(): TestGenerator {
    return addGenerator(ConjunctionIdentityTests())
}

//Implements the "identity" rule A && B <=> A && B && B
class ConjunctionIdentityTests : TestRule {
    override fun getTests(system: System): List<Test> = sequence {
        if (system !is Conjunction)
            return@sequence

        /*for (child in system.children) {
            yield(createSelfRefinementTest(system, child,false)) //  A && B <= (A && B) && B
            yield(createSelfRefinementTest(system, child,true))  // (A && B) && B <= A && B
        }*/

        yield(createSelfRefinementTest(system, system,false)) // A && B <= (A && B) && (A && B)
        yield(createSelfRefinementTest(system, system,true))  // (A && B) && (A && B) <= A && B
    }.toList()

    private fun createSelfRefinementTest(system: Conjunction, other: System, flip: Boolean) : Test {
        val first = system.getName()
        val second = "${system.getName()} && ${other.getName()}"
        return ImplicationTest(SatisfiedTest(
            "ConjIdentityRefinement::Precondition",
            system.getProjectFolder(),
            "consistency: $first"
        ), SatisfiedTest(
            "ConjIdentityRefinement",
            system.getProjectFolder(),
            "refinement: ${if (flip) first else second} <= ${if (flip) second else first}"
        ))

    }

}