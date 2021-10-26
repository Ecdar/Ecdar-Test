package tests

import QueryResultReader
import TestResult

//This test acts like the implication logical operator i.e. precondition -> mainTest
class ImplicationTest(private val precondition: Test, private val mainTest: Test) :
    Test(mainTest.testSuite, mainTest.projectPath, "${precondition.query};${mainTest.query}") {

    override fun getResult(resultReader: QueryResultReader): TestResult {
        if (precondition.getResult(resultReader).result) {
            return mainTest.getResult(resultReader)
        }

        val result = mainTest.getResult(resultReader)
        return TestResult(this, true, result.engineOutput)
    }
}   