import parsing.EngineConfiguration
import parsing.GeneralConfiguration
import parsing.System
import tests.Test
import tests.testgeneration.*

class TestGenerator {
    private val testGenerators = ArrayList<TestRule>()

    fun addGenerator(generator: TestRule): TestGenerator {
        testGenerators.add(generator)
        return this
    }

    fun generateTests(systems: ArrayList<System>, count: Int?): Collection<Test> {
        val generatedTests = ArrayList<Test>()

        for (testGen in testGenerators) {
            val generatedIntermediate = ArrayList<Test>()
            for (system in systems) {
                val newTests = testGen.getTests(system)
                generatedIntermediate.addAll(newTests)
            }
            if (count != null)
                generatedTests.addAll(generatedIntermediate.take(count / NUMBER_OF_DIFFERENT_TESTS))
            else
                generatedTests.addAll(generatedIntermediate)
        }

        return generatedTests
    }
}