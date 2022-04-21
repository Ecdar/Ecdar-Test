package tests

import TestResult

abstract class Test(val testSuite: String, val projectPath: String) {
    val type = this.javaClass.simpleName
    abstract fun queries(): List<String>
    abstract fun toSingleTest(): SingleTest
}

open class SingleTest(testSuite: String, projectPath: String, val query: String) : Test(testSuite, projectPath) {
    open fun getResult(success: Boolean): TestResult { throw error("Unimplemented")}
    override fun queries(): List<String> {
        return listOf(query)
    }

    override fun toSingleTest(): SingleTest {
        return this
    }
}

abstract class MultiTest(testSuite: String, projectPath: String, val query: String, vararg val tests: Test) : Test(testSuite, projectPath) {
    abstract fun getResult(results: List<TestResult>) : TestResult
    override fun queries(): List<String> {
        return tests.flatMap { it.queries() }
    }
}