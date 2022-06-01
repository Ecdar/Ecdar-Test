package tests

import TestResult

//This test acts like the implication logical operator i.e. precondition -> mainTest
class ImplicationTest(precondition: SingleTest, mainTest: SingleTest) :
    MultiTest(
        mainTest.testSuite,
        mainTest.projectPath,
        "(${precondition.query}) implies (${mainTest.query})",
        precondition, mainTest
    ) {

    override fun toSingleTest(): SingleTest {
        return SatisfiedTest(this.testSuite, this.projectPath, this.query)
    }

    override fun getResult(results: List<TestResult>): TestResult {
        assert(results.size == 2)

        val pre = results[0]
        val post = results[1]

        if (pre.result == ResultType.EXCEPTION || post.result == ResultType.EXCEPTION) {
            val result = TestResult(this.toSingleTest(), ResultType.EXCEPTION, ResultType.NON_EXCEPTION, listOf(pre, post))
            result.exception = if (pre.result == ResultType.EXCEPTION) {
                pre.exception
            } else {
                post.exception
            }

            return result
        }

        if (pre.result == pre.expected) {
            return TestResult(this.toSingleTest(), post.result, post.expected, listOf(pre, post))
        }

        return TestResult(this.toSingleTest(), ResultType.NON_EXCEPTION, ResultType.NON_EXCEPTION, listOf(pre, post))
    }
}   