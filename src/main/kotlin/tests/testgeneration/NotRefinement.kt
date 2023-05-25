package tests.testgeneration

import TestGenerator
import parsing.System
import tests.NotSatisfiedTest
import tests.Test

fun TestGenerator.addNotRefinesTests(): TestGenerator {
    return addGenerator(NotRefinement())
}

class NotRefinement : TestRule {

    override fun getTests(system: System): List<Test> =
        sequence<Test> {
                for (other in system.thisNotRefines) {
                    yield(createNonRefinementTest(system, other))
                }
            }
            .toList()

    private fun createNonRefinementTest(system: System, other: System) =
        NotSatisfiedTest(
            "NotRefines",
            system.getProjectFolder(),
            "refinement: ${system.getName()} <= ${other.getName()}")
}
