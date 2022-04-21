package tests

import TestResult

class NotSatisfiedTest(testSuite: String, projectPath: String, query: String) : SingleTest(testSuite, projectPath, query) {
    override fun getResult(success: Boolean): TestResult {
        return TestResult(this, ResultType.from(success), ResultType.UNSATISFIED, listOf())
    }
}