import facts.RelationLoader
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.function.Executable
import parsing.EngineConfiguration
import parsing.parseEngineConfigurations
import proofs.addAllProofs
import tests.Test
import tests.testgeneration.addAllTests

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GeneratedTests {

    val engines = parseEngineConfigurations()

    @org.junit.jupiter.api.Test
    fun test() {
        assertTrue(true)
    }

    @TestFactory
    fun main(): Iterable<DynamicTest?> {
        val tests = generateTests()

        return createExecutableJUnitTests(engines, tests)
    }

    private fun generateTests(): Collection<Test> {
        val initialRelations = RelationLoader.relations
        val allRelations = ProofSearcher().addAllProofs().findNewRelations(initialRelations)
        return TestGenerator().addAllTests().generateTests(allRelations)
    }

    private fun createExecutableJUnitTests(
        engines: Collection<EngineConfiguration>,
        tests: Collection<Test>
    ): ArrayList<DynamicTest> {
        val dynamicTests = ArrayList<DynamicTest>()

        for (engine in engines) {
            val executor = Executor(engine, engine.addresses[0], engine.port)
            for (test in tests) {
                val jUnitTest = createJUnitTest(executor, test)
                dynamicTests.add(jUnitTest)
            }
            executor.terminate()
        }

        return dynamicTests
    }

    private fun createJUnitTest(executor: Executor, test: Test): DynamicTest {
        val testName =
            "${executor.name}::${test.testSuite}::${test.projectPath}::${test.queries().joinToString("; ")}"
        val testBody = Executable {
            println("Testing $testName")
            val t = executor.runTest(test)
            assertEquals(t.expected, t.result, "Test failed: $testName")
        }
        return dynamicTest(testName, testBody)
    }
}
