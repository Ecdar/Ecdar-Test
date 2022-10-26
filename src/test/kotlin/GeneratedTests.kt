import facts.RelationLoader
import org.junit.jupiter.api.AfterAll
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
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GeneratedTests {

    val engines = parseEngineConfigurations()

    @AfterAll
    fun cleanup() {
        println("Cleaning up")
        engines.forEach { it.terminate() }
    }


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
            val executor = Executor(engine)
            for (test in tests) {
                val jUnitTest = createJUnitTest(executor, test, engine.deadline)
                dynamicTests.add(jUnitTest)
            }
        }

        return dynamicTests
    }

    private fun createJUnitTest(executor: Executor, test: Test, deadline: Long?): DynamicTest {
        val testName = "${executor.engineConfig.name}::${test.testSuite}::${test.projectPath}::${test.queries().joinToString("; ")}"
        val testBody = Executable {
            println("Testing $testName")
            val t = executor.runTest(test, deadline)
            assertEquals(t.expected, t.result, "Test failed: $testName") }
        return dynamicTest(testName, testBody)
    }

    protected fun finalize() {
        println("Forcefully terminating all processes")
        engines.forEach { it.terminate() }
    }
}