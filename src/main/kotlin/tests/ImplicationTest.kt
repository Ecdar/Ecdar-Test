package tests

import TestResult

//This test acts like the implication logical operator i.e. precondition -> mainTest
class ImplicationTest(precondition: SingleTest, mainTest: SingleTest, relatedProofs: Int) :
    MultiTest(
        mainTest.testSuite,
        mainTest.projectPath,
        "(${precondition.query}) implies (${mainTest.query})",
        relatedProofs, precondition, mainTest
    ) {

    override fun toSingleTest(): SingleTest {
        return SatisfiedTest(this.testSuite, this.projectPath, this.query, this.relatedProofs)
    }

    override fun getResult(results: List<TestResult>): TestResult {
        assert(results.size == 2)

        val pre = results[0]
        val post = results[1]

        if (pre.result == ResultType.EXCEPTION) {
            val result = TestResult(this.toSingleTest(), ResultType.EXCEPTION, ResultType.NON_EXCEPTION, listOf(pre, post))
            result.exception = pre.exception

            return result
        }

        if (pre.result == pre.expected) {
            val result = TestResult(this.toSingleTest(), post.result, post.expected, listOf(pre, post))
            result.exception = post.exception
            return result
        }

        return TestResult(this.toSingleTest(), ResultType.NON_EXCEPTION, ResultType.NON_EXCEPTION, listOf(pre, post))
    }
}   