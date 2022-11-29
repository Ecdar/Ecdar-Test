package tests.testgeneration

import TestGenerator
import parsing.System
import tests.NotSatisfiedTest
import tests.SatisfiedTest
import tests.Test

fun TestGenerator.addConsistencyTests(): TestGenerator {
    return addGenerator(ConsistencyTests())
}

class ConsistencyTests : TestRule {
    override fun getTests(system: System): List<Test> = sequence {
        if (system.isLocallyConsistent.isPresent) {
            yield(createTest(system))
        }
    }.toList()

    private fun createTest(system: System): Test {
        return if (system.isKnownLocallyConsistent()) {
            createConsistencyTest(system)
        } else {
            createNonConsistencyTest(system)
        }
    }

    private fun createConsistencyTest(system: System) =
        SatisfiedTest(
            "Consistency",
            system.getProjectFolder(),
            "consistency: ${system.getName()}",
            system.relatedProofs
        )

    private fun createNonConsistencyTest(system: System) =
        NotSatisfiedTest(
            "NonConsistency",
            system.getProjectFolder(),
            "consistency: ${system.getName()}",
            system.relatedProofs
        )
}