package parsing

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import java.io.File
import java.io.IOException

fun parseEngineConfigurations(): List<EngineConfiguration> {
    return ArrayList<EngineConfiguration>(Klaxon().parseArray(File("configuration.json"))!!).filter { it.enabled }
}

data class EngineConfiguration(
    val enabled: Boolean,
    val name: String,
    @Json(name = "executablePath")
    val path: String,
    val parameterExpression: String,
    val ip: String,
    val port: Int,
    val processes: Int,
    val addresses: List<String> = (port until port + processes).map { "${ip}:$it" },
    val ports: List<Int> = (port until port + processes).toList(),
) {
    var procs : MutableList<Process>? = null

    fun initialize() {
        if (procs==null) {
            procs = ports.map{
                processFromPort(it)
            }.toMutableList()
        }
    }

    private fun processFromPort(port: Int): Process {
        val pb = ProcessBuilder(path,
            parameterExpression
                .replace("{port}", port.toString())
                .replace("{ip}", ip)
        )
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
        pb.directory(File(path).parentFile) // Set the working directory for dll location

        while (true) {
            try {
                //println("Starting process on port $port")
                val proc = pb.start()
                proc.pid()
                return proc
            } catch (e: IOException) {
                println("Process reboot failed, retrying...")
                Thread.sleep(1_000)
            }
        }
    }

    fun terminate() {
        procs!!.forEach { it.destroy()}
    }

    fun reset(id: Int) {
        procs!![id].destroyForcibly().waitFor()
        procs!![id] = processFromPort(ports[id])
    }

    fun getCommand(folder: String, query: String): String {
        return "$path ${parameterExpression.replace("{input}", folder).replace("{query}", query)}"
    }
}


