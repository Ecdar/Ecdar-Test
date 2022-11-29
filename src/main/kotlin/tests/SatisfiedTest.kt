package tests

import TestResult

class SatisfiedTest(testSuite: String, projectPath: String, query: String, relatedProofs: Int)
    : SingleTest(testSuite, projectPath, query, relatedProofs) {
    override fun getResult(success: Boolean): TestResult {
        return TestResult(this, ResultType.from(success), ResultType.SATISFIED, listOf())
    }
}