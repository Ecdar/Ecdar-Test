import parsing.RelationParserFacade
import parsing.RelationVisitor
import parsing.System

fun proofSearchContains(
    initialFactSheet: String,
    expectedFactSheetSubset: String,
    proofSearcher: ProofSearcher
): Boolean {
    val initial = parseFactSheet(initialFactSheet)
    val resultSubset = parseFactSheet(expectedFactSheetSubset)

    val result = proofSearcher.findNewRelations(initial)

    return isSubset(result, resultSubset)
}

fun parseFactSheet(factSheet: String): ArrayList<System> {
    val parse = RelationParserFacade.parse(factSheet)!!
    val visitor = RelationVisitor()
    parse.accept(visitor)
    return visitor.components
}

fun isSubset(full: ArrayList<System>, subset: ArrayList<System>): Boolean {
    var result = true
    for (expectedComp in subset) {

        for (comp in full) {
            if (!expectedComp.sameAs(comp)) {
                continue
            }
            if (!(relationSubset(
                comp.notRefinesThis,
                expectedComp.notRefinesThis,
                "Not Refined by",
                expectedComp) and
                relationSubset(
                    comp.thisNotRefines,
                    expectedComp.thisNotRefines,
                    "Not Refines",
                    expectedComp) and
                relationSubset(
                    comp.refinesThis, expectedComp.refinesThis, "Refined by", expectedComp) and
                relationSubset(
                    comp.thisRefines, expectedComp.thisRefines, "Refines", expectedComp))) {
                result = false
            }

            if (expectedComp.isLocallyConsistent.isPresent) {
                if (!comp.isLocallyConsistent.isPresent ||
                    comp.isLocallyConsistent.get() != expectedComp.isLocallyConsistent.get()) {
                    println(
                        "Consistency not the same ${expectedComp.isLocallyConsistent} subset of ${comp.isLocallyConsistent}")
                    result = false
                }
            }
        }
    }

    return result
}

fun relationSubset(
    full: Collection<System>,
    subset: Collection<System>,
    relationType: String,
    from: System
): Boolean {
    var result = true
    for (expected in subset) {
        var hasElement = false
        for (element in full) {
            if (expected.sameAs(element)) {
                hasElement = true
            }
        }
        if (!hasElement) {
            println(
                "Couldnt find the relation ${from.toString()} $relationType ${expected.toString()}")
            result = false
        }
    }
    return result
}
