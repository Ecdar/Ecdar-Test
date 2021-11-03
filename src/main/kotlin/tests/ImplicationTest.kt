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
        val post = results[0]

        if (pre.result) {
            return post
        }

        return TestResult(this, true)
    }
}   