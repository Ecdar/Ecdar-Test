package tests

import QueryResultReader
import TestResult

abstract class Test(val testSuite: String, val projectPath: String) {
    abstract fun queries(): List<String>
}

abstract class SingleTest(testSuite: String, projectPath: String, val query: String) : Test(testSuite, projectPath) {
    abstract fun getResult(success: Boolean): TestResult
    override fun queries(): List<String> {
        return listOf(query)
    }
}

abstract class MultiTest(testSuite: String, projectPath: String, vararg val tests: Test) : Test(testSuite, projectPath) {
    abstract fun getResult(results: List<TestResult>) : TestResult
    override fun queries(): List<String> {
        return tests.flatMap { it.queries() }
    }
}