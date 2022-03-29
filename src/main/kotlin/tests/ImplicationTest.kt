package tests

import QueryResultReader
import TestResult

//This test acts like the implication logical operator i.e. precondition -> mainTest
class ImplicationTest(precondition: SingleTest, mainTest: SingleTest) :
    MultiTest(
        mainTest.testSuite,
        mainTest.projectPath,
        precondition, mainTest
    ) {

    override fun getResult(results: List<TestResult>): TestResult {
        assert(results.size == 2)

        val pre = results[0]
        val post = results[1]

        if (pre.result == ResultType.EXCEPTION) {
            return TestResult(this, ResultType.EXCEPTION, post.expected)
        }

        if (pre.result == pre.expected) {
            return post
        }

        return TestResult(this, ResultType.NON_EXCEPTION, ResultType.NON_EXCEPTION)
    }
}   