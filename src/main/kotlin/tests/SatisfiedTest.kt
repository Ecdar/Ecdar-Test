package tests

import QueryResultReader
import TestResult

class SatisfiedTest(testSuite: String, projectPath: String, query: String) : SingleTest(testSuite, projectPath, query) {
    override fun getResult(success: Boolean): TestResult {
        return TestResult(this, ResultType.from(success), ResultType.SATISFIED)
    }
}