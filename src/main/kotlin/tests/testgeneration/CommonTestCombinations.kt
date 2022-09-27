package tests.testgeneration

import TestGenerator


public const val NUMBER_OF_DIFFERENT_TESTS = 4 // Number from below
fun TestGenerator.addAllTests(): TestGenerator {
    this.addNotRefinesTests()
        .addRefinementTests()
        .addConsistencyTests()
        .addImpliedRefinementTests()
        //.addConjunctionIdentityTests()
        //.addConjunctionCommutativeTests()

    return this
}