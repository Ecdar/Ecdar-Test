package tests.testgeneration

import parsing.System
import tests.Test

interface TestRule {
    fun getTests(system: System): List<Test>
}
