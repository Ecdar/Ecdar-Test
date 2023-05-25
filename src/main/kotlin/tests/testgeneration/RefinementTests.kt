package tests.testgeneration

import TestGenerator
import parsing.System
import tests.SatisfiedTest
import tests.Test

fun TestGenerator.addRefinementTests(): TestGenerator {
    return addGenerator(RefinementTests())
}

class RefinementTests : TestRule {
    override fun getTests(system: System): List<Test> =
        sequence {
                for (other in system.thisRefines) {
                    if (!system.isKnownLocallyConsistent() || !other.isKnownLocallyConsistent())
                        continue

                    yield(createTest(system, other))
                }
            }
            .toList()

    private fun createTest(system: System, other: System): Test {
        return if (system == other) {
            createSelfRefinementTest(system)
        } else {
            createRefinementTest(system, other)
        }
    }

    private fun createRefinementTest(system: System, other: System) =
        SatisfiedTest(
            "Refinement",
            system.getProjectFolder(),
            "refinement: ${system.getName()} <= ${other.getName()}")

    private fun createSelfRefinementTest(system: System) =
        SatisfiedTest(
            "SelfRefinement",
            system.getProjectFolder(),
            "refinement: ${system.getName()} <= ${system.getName()}")
}
