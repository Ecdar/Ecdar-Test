package parsing

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import java.io.File
import java.io.IOException
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicBoolean


fun parseEngineConfigurations(): List<EngineConfiguration> {
    return ArrayList<EngineConfiguration>(Klaxon().parseArray(File("configuration.json"))!!).filter { it.enabled }
}

data class EngineConfiguration (
    val enabled: Boolean,
    val name: String,
    val version: String,
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
    var alive : AtomicBoolean = AtomicBoolean(true)

    fun initialize() {


        if (procs==null) {
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
            parameterExpression
                .replace("{port}", port.toString())
                .replace("{ip}", ip)
        )
            //.redirectOutput(ProcessBuilder.Redirect.appendTo(File("Engine-$name-log.txt")))
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
        pb.directory(File(path).parentFile) // Set the working directory for dll location

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
        //println("Resetting process $id")
        destroy(procs!![id].toHandle())
        procs!![id] = processFromPort(ports[id])
    }

    fun getCommand(folder: String, query: String): String {
        return "$path ${parameterExpression.replace("{input}", folder).replace("{query}", query)}"
    }

}


