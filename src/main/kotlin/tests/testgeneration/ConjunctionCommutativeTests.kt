package tests.testgeneration

import TestGenerator
import parsing.Conjunction
import parsing.System
import tests.SatisfiedTest
import tests.Test
import java.util.Collections.swap

fun TestGenerator.addConjunctionCommutativeTests(): TestGenerator {
    return addGenerator(ConjunctionCommutativeTests())
}

fun <V> List<V>.permutations(): List<List<V>> {
    val retVal: MutableList<List<V>> = mutableListOf()

    fun generate(k: Int, list: List<V>) {
        // If only 1 element, just output the array
        if (k == 1) {
            retVal.add(list.toList())
        } else {
            for (i in 0 until k) {
                generate(k - 1, list)
                if (k % 2 == 0) {
                    swap(list, i, k - 1)
                } else {
                    swap(list, 0, k - 1)
                }
            }
        }
    }

    generate(this.count(), this.toList())
    return retVal
}

class ConjunctionCommutativeTests : TestRule {
    override fun getTests(system: System): List<Test> = sequence {
        if (!system.isKnownLocallyConsistent() || system !is Conjunction)
            return@sequence
        for (permutation in system.children.toList().permutations()){
            yield(createSelfRefinementTest(system, permutation, false))
            yield(createSelfRefinementTest(system, permutation, true))
        }
    }.toList()

    private fun createSelfRefinementTest(original: System, permutation: List<System>, flip: Boolean) : SatisfiedTest {
        val first = "(${permutation.joinToString(" && ") { child -> child.getName()}})"
        val second = original.getName()

        return SatisfiedTest(
            "ConjCommutativeRefinement",
            original.getProjectFolder(),
            "refinement: ${if (flip) first else second} <= ${if (flip) second else first}",
            original.relatedProofs
        )
    }
}