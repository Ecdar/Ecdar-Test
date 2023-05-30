package analytics

import kotlin.reflect.KType
import kotlin.reflect.full.createType
import parsing.System
import proofs.Proof

class ComponentsAdded(private var proofType: ArrayList<KType>) : ProofAnalytic {

    var componentsCreated = 0
    var createdPerIteration = ArrayList<Int>()

    private var componentCount = 0

    override fun recordStart(components: ArrayList<System>) {
        componentCount = components.count()
    }

    override fun recordProofIteration(
        dirtyComponents: HashSet<System>,
        components: ArrayList<System>,
        proof: Proof
    ) {
        val next = components.count()
        if (proofType.contains(proof.javaClass.kotlin.createType())) {
            createdPerIteration.add(next - componentCount)
            componentsCreated += next - componentCount
        }
        componentCount = next
    }

    fun printFindings() {
        println("In total $componentsCreated systems were added")
        println("Iteration breakdown")

        var iterationCount = 1
        for (created in createdPerIteration) {
            println("Iteration $iterationCount: $created")
            iterationCount++
        }
    }
}
