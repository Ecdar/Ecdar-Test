package tests.testgeneration

import TestGenerator
import parsing.System
import tests.ImplicationTest
import tests.SatisfiedTest
import tests.Test

fun TestGenerator.addImpliedRefinementTests(): TestGenerator {
    return addGenerator(ImpliedRefinementTest())
}

class ImpliedRefinementTest: TestRule {
    override fun getTests(system: System): List<Test> = sequence<Test> {
        if (system.isLocallyConsistent.isPresent)
            return@sequence

        yield(createSelfTest(system)) //Self refinement

        for (other in system.refinesThis){
            if (other.isKnownLocallyConsistent()){
                yield(createTest(other, system, system))
            }
        }

        for (other in system.thisRefines){
            if (other.isKnownLocallyConsistent()){
                yield(createTest(system, other, system))
            }
        }
    }.toList()

    private fun createSelfTest(system: System) : Test {
        val precondition = createPreconditionTest(system)
        val mainTest = createSelfRefinementTest(system)

        return ImplicationTest(precondition, mainTest)
    }

    private fun createTest(lhs: System, rhs: System, preconditionSystem: System): Test {
        val precondition = createPreconditionTest(preconditionSystem)
        val mainTest = createRefinementTest(lhs, rhs)

        return ImplicationTest(precondition, mainTest)
    }

    private fun createPreconditionTest(system: System) =
        SatisfiedTest(
            "ImpliedRefinement::Precondition",
            system.getProjectFolder(),
            "consistency: ${system.getName()}"
        )

    private fun createSelfRefinementTest(system: System) =
        SatisfiedTest(
            "ImpliedSelfRefinement",
            system.getProjectFolder(),
            "refinement: ${system.getName()} <= ${system.getName()}"
        )

    private fun createRefinementTest(lhs: System, rhs: System) =
        SatisfiedTest(
            "ImpliedRefinement",
            lhs.getProjectFolder(),
            "refinement: ${lhs.getName()} <= ${rhs.getName()}"
        )


}