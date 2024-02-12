package parsing

import EcdarProtoBuf.QueryProtos.QueryRequest.Settings
import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import java.io.File

fun parseEngineConfigurations(): List<EngineConfiguration> =
    ArrayList<EngineConfiguration>(Klaxon().parseArray(File("configuration.json"))!!).filter {
        it.enabled || it.testsSavePath != null
    }

data class EngineConfiguration(
    val enabled: Boolean,
    val name: String,
    val version: String,
    @Json(name = "executablePath", serializeNull = false) val path: String?,
    @Json(serializeNull = false) val parameters: Array<String>?,
    val ip: String,
    val port: Int,
    @Json(serializeNull = false) val verbose: Boolean?,
    val processes: Int,
    val addresses: List<String> = (port until port + processes).map { "${ip}:$it" },
    val ports: List<Int> = (port until port + processes).toList(),
    @Json(serializeNull = false) val testCount: Int?,
    @Json(serializeNull = false) val testSorting: Sorting?,
    @Json(serializeNull = false) val queryComplexity: Array<Int>?,
    @Json(name = "testTimeout", serializeNull = false) val deadline: Long = 30,
    @Json(serializeNull = false) val testsSavePath: String?,
    @Json(name = "gRPCSettings", serializeNull = false)
    private val _settings: DummySettings = DummySettings(),
    @Json(ignored = true) val settings: Settings = Settings(_settings),
) {
    fun bounds(): Pair<Int, Int>? =
        this.queryComplexity?.let {
            val lower: Int = it.getOrNull(0) ?: 0
            val upper: Int = it.getOrNull(1) ?: Int.MAX_VALUE
            assert(upper < lower) {
                "The upper bound for `queryComplexity` can't be less than the lower bound; Lower: $lower, upper: $upper"
            }
            Pair(lower, upper)
        }
}

fun Settings(dummy: DummySettings): Settings =
    Settings.newBuilder().setDisableClockReduction(dummy.disableClockReduction).build()

data class DummySettings(
    @Json(name = "disable-clock-reduction", serializeNull = false)
    val disableClockReduction: Boolean = true,
)

enum class Sorting {
    Random,
    FILO,
    FIFO,
    @Json(name = "Split") RoundRobin
}
