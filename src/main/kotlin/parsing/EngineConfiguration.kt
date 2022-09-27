package parsing

import com.beust.klaxon.Json
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicBoolean


fun parseEngineConfigurations(): List<Configuration> {
    val parser = Klaxon()
    return ArrayList(parser.parseJsonArray(FileReader("configuration.json")).map {
        try {
            parser.parseFromJsonObject<GeneralConfiguration>(it as JsonObject)!!
        } catch (e: Exception) {
            parser.parseFromJsonObject<EngineConfiguration>(it as JsonObject)!!
        }
    })
}
interface Configuration {
    //val name: String
}

data class GeneralConfiguration(
    //val name: String,
    @Json(serializeNull = false)
    val testCount: Int?,
    @Json(serializeNull = false)
    val queryComplexity: Int?,
) : Configuration

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
) : Configuration {
    var procs : MutableList<Process>? = null
    var alive : AtomicBoolean = AtomicBoolean(true)

    fun initialize() {
        if (procs==null) {
            if (isExternal()) {
                println("Expecting external engine on address $ip:$port due to missing executable path and parameter expression in engine configuration...")
                return
            }
            procs = ports.map{
                processFromPort(it)
            }.toMutableList()

            // Add shutdown hook to clean up processes
            class ShutDownTask : Thread() {
                override fun run() {
                    alive.set(false)
                    sleep(100)
                    //println("Shutting down...")
                    terminate()
                }
            }

            Runtime.getRuntime().addShutdownHook(ShutDownTask())

            Thread.sleep(3_000)
        }


    }

    private fun processFromPort(port: Int): Process {
        val pb = ProcessBuilder(path,
            parameterExpression!!
                .replace("{port}", port.toString())
                .replace("{ip}", ip)
        )
            //.redirectOutput(ProcessBuilder.Redirect.appendTo(File("Engine-$name-log.txt")))
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
        pb.directory(File(path!!).parentFile) // Set the working directory for dll location

        while (true) {
            while(!isLocalPortFree(port)) {
                //println("Waiting for port $port to be available...")
                Thread.sleep(1_000)
            }
            try {
                if (!alive.get()) { return null!! }
                //println("Starting process on port $port")

                val proc = pb.start()
                proc.pid()
                //Thread.sleep(500)
                return proc
            } catch (e: IOException) {
                println("Process failed to boot, retrying...")
                Thread.sleep(1_000)
            }
        }
    }

    private fun isLocalPortFree(port: Int): Boolean {
        return try {
            ServerSocket(port).close()
            true
        } catch (e: IOException) {
            false
        }
    }

    fun terminate() {
        if (isExternal()) {
            return
        }
        procs!!.forEach {
            destroy(it.toHandle())
        }
    }

    private fun destroy(proc: ProcessHandle) {
        proc.descendants().forEach{
            destroy(it)
        }
        proc.destroy()
        while (proc.isAlive) {
            Thread.sleep(100)
        }
    }

    fun reset(id: Int) {
        if (isExternal()) {
            throw Exception("Tried to reset external engine process")
        }
        //println("Resetting process $id")
        destroy(procs!![id].toHandle())
        procs!![id] = processFromPort(ports[id])
    }

    fun isExternal(): Boolean {
        return path == null || parameterExpression == null
    }

}


