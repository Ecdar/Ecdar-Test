package parsing

import EcdarProtoBuf.QueryProtos.QueryRequest.Settings
import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import java.io.File
import java.io.IOException
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicBoolean


fun parseEngineConfigurations(): List<EngineConfiguration>
    = ArrayList<EngineConfiguration>(Klaxon().parseArray(File("configuration.json"))!!).filter { it.enabled || it.testsSavePath != null}

data class EngineConfiguration (
    val enabled: Boolean,
    val name: String,
    val version: String,
    @Json(name = "executablePath", serializeNull = false)
    val path: String?,
    @Json(serializeNull = false)
    val parameterExpression: String?,
    val ip: String,
    val port: Int,
    @Json(serializeNull = false)
    val verbose: Boolean?,
    val processes: Int,
    val addresses: List<String> = (port until port + processes).map { "${ip}:$it" },
    val ports: List<Int> = (port until port + processes).toList(),
    @Json(serializeNull = false)
    val testCount: Int?,
    @Json(serializeNull = false)
    val testSorting: Sorting?,
    @Json(serializeNull = false)
    val queryComplexity: Array<Int>?,
    @Json(name = "testTimeout", serializeNull = false)
    val deadline: Long = 30,
    @Json(serializeNull = false)
    val testsSavePath: String?,
    @Json(name = "gRPCSettings", serializeNull = false)
    private val _settings: DummySettings = DummySettings(),
    @Json(ignored = true)
    val settings: Settings = _settings.getSettings(),
) {
    fun bounds(): Pair<Int, Int> {
        val upper: Int; val lower: Int
        if (this.queryComplexity!!.size >= 2) {
            upper = this.queryComplexity[1]
            lower = this.queryComplexity.first()
        } else {
            upper = this.queryComplexity.firstOrNull() ?: Int.MAX_VALUE
            lower = 0
        }

        if (upper < lower)
            throw Exception("The upper bound for `queryComplexity` can't be less than the lower bound")

        return Pair(lower, upper)
    }
}

data class DummySettings(
    @Json(name = "disable-clock-reduction", serializeNull = false)
    val disableClockReduction: Boolean = true,
) {
    fun getSettings(): Settings = Settings.newBuilder()
        .setDisableClockReduction(disableClockReduction)
        .build()
}

enum class Sorting {
    Random,
    FILO,
    FIFO,
    @Json(name = "Split")
    RoundRobin
}
