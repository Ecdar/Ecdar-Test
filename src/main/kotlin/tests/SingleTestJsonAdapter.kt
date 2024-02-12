package tests

import com.beust.klaxon.TypeAdapter
import kotlin.reflect.KClass

class SingleTestJsonAdapter : TypeAdapter<SingleTest> {
    override fun classFor(type: Any): KClass<out SingleTest> =
        when (type as String) {
            "NotSatisfiedTest" -> NotSatisfiedTest::class
            "SatisfiedTest" -> SatisfiedTest::class
            else -> throw IllegalArgumentException("Unknown type: $type")
        }
}
