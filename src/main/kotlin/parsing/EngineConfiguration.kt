package parsing

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import java.io.File

fun parseEngineConfigurations(): List<EngineConfiguration> {
    return Klaxon().parseArray(File("configuration.json"))!!
}

data class EngineConfiguration(
    val name: String,
    @Json(name = "executablePath")
    val path: String,
    @Json(name = "parameterExpression")
    val parameterExpression: String,
    @Json(name = "ip")
    val ip: String,
    @Json(name = "port")
    val port: Int,
    @Json(name = "processes")
    val processes: Int,
    val addresses: List<String> = (port until port+processes).map { "${ip}:$it" }
) {
    var procs : List<Process>? = null

    init {
        procs = addresses.map{
         ProcessBuilder(path, parameterExpression.replace("{address}", it))
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
        }
    }

    fun terminate() {
        procs?.forEach { it.destroyForcibly() }
    }

    fun getCommand(folder: String, query: String): String {
        return "$path ${parameterExpression.replace("{input}", folder).replace("{query}", query)}"
    }
}


