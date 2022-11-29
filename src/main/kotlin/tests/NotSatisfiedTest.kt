package tests

import TestResult

class NotSatisfiedTest(testSuite: String, projectPath: String, query: String, relatedProofs: Int)
    : SingleTest(testSuite, projectPath, query, relatedProofs) {
    override fun getResult(success: Boolean): TestResult {
        return TestResult(this, ResultType.from(success), ResultType.UNSATISFIED, listOf())
    }
}