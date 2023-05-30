package tests

import EcdarProtoBuf.QueryProtos
import TestResult

class SatisfiedTest(testSuite: String, projectPath: String, query: String) : SingleTest(testSuite, projectPath, query) {
    override fun getResult(success: Boolean): TestResult =
        TestResult(this, ResultType.from(success), ResultType.SATISFIED, listOf())

    override fun getResult(result: QueryProtos.QueryResponse.ResultCase): TestResult =
    TestResult(this, ResultType.from(result), ResultType.SATISFIED, listOf())

}