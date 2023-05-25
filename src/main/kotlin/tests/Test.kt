package tests

import EcdarProtoBuf.QueryProtos.QueryResponse.ResultCase
import TestResult

abstract class Test(val testSuite: String, val projectPath: String) {
    val type = this.javaClass.simpleName

    abstract fun queries(): List<String>

    abstract fun toSingleTest(): SingleTest

    fun format(indenter: String): String {
        val sep = System.lineSeparator()
        var out = "$indenter${this.type}: ${this.testSuite}$sep"
        for (query in this.queries()) {
            out += "$indenter$indenter\"${query}\"$sep"
        }
        return out
    }
}

abstract class SingleTest(testSuite: String, projectPath: String, val query: String) :
    Test(testSuite, projectPath) {
    abstract fun getResult(success: Boolean): TestResult

    abstract fun getResult(result: ResultCase): TestResult

    override fun queries(): List<String> {
        return listOf(query)
    }

    override fun toSingleTest(): SingleTest {
        return this
    }
}

abstract class MultiTest(
    testSuite: String,
    projectPath: String,
    val query: String,
    vararg val tests: Test
) : Test(testSuite, projectPath) {
    abstract fun getResult(results: List<TestResult>): TestResult

    override fun queries(): List<String> {
        return tests.flatMap { it.queries() }
    }
}
