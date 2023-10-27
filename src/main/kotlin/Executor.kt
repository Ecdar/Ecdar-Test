import EcdarProtoBuf.ComponentProtos
import EcdarProtoBuf.EcdarBackendGrpc
import EcdarProtoBuf.QueryProtos
import EcdarProtoBuf.QueryProtos.QueryResponse.ResultCase
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import parsing.EngineConfiguration
import tests.MultiTest
import tests.SingleTest
import tests.Test
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.ServerSocket
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


class Executor(engine: EngineConfiguration, address: String, private var port: Int) {
    val name = engine.name
    private var queryId = AtomicInteger(0)
    private var proc: Process? = null
    private val stub =
        EcdarBackendGrpc.newBlockingStub(
            ManagedChannelBuilder.forTarget(address).usePlaintext().build())

    private val deadline: Long = engine.deadline
    private val settings: QueryProtos.QueryRequest.Settings = engine.settings
    private val verbose = engine.verbose ?: true
    private val path = engine.path!!
    private var params = engine.parameters!!
    private val ip = engine.ip

    init {
        initialize()
    }

    fun execute(
        tests: Collection<Test>,
        results: ConcurrentLinkedQueue<TestResult>,
        progress: AtomicInteger,
        failedTests: AtomicInteger
    ) {
        tests.forEach {
            val testResult = runTest(it)

            results.add(testResult)

            progress.getAndAdd(1)

            if (testResult.result != testResult.expected) {
                if (verbose) {
                    print("\r") // Replace the progress bar
                    printTestResult(testResult)
                }
                failedTests.getAndAdd(1)
            }
        }
        terminate()
    }

    private inline fun <T> List<T>.sumOf(selector: (T) -> Double?): Double? =
        this.map { selector.invoke(it) ?: return null }.sum()

    fun runTest(test: Test): TestResult {
        try {
            if (test is MultiTest) {
                val resses = test.tests.map { runTest(it) }
                val res = test.getResult(resses)
                res.time = resses.sumOf { it.time }
                return res
            } else if (test is SingleTest) {
                // lock()
                val queryId = queryId.getAndAdd(1)
                val success: ResultCase?
                val start = System.currentTimeMillis()
                val query =
                    QueryProtos.QueryRequest.newBuilder()
                        .setQueryId(queryId)
                        .setQuery(test.query)
                        .setComponentsInfo(componentUpdateFromPath(test.projectPath))
                        .setSettings(settings)
                        .build()

                val result =
                    try {
                        sendQuery(query)
                    } catch (e: StatusRuntimeException) {
                        if (e.status.code == io.grpc.Status.Code.UNAVAILABLE) {
                            resetStub()
                            sendQuery(query)
                        } else throw e
                    } catch (e: IOException) {
                        resetStub()
                        sendQuery(query)
                    }

                success =
                    when (val r = result.resultCase) {
                        ResultCase.PARSING_ERROR ->
                            throw Exception(
                                "Query: ${test.query} in ${test.projectPath} lead to parsing-error: ${result.error}")
                        ResultCase.ERROR ->
                            throw Exception(
                                "Query: ${test.query} in ${test.projectPath} lead to error: ${result.error}")
                        ResultCase.RESULT_NOT_SET ->
                            throw Exception(
                                "Query: ${test.query} in ${test.projectPath} could not produce result. Error: ${result.error}")
                        else -> r
                    }

                val res = test.getResult(success!!)
                res.time = (System.currentTimeMillis() - start).toDouble()
                return res
            }
        } catch (e: Throwable) {
            if (test is MultiTest) throw Exception("Did not except MultiTest to throw error $e")
            val r =
                TestResult(
                    test.toSingleTest(), ResultType.EXCEPTION, ResultType.NON_EXCEPTION, listOf())
            r.exception = e.toString()
            return r
        }
        throw Exception("Cannot execute test of type ${test.javaClass.name}")
    }

    private fun sendQuery(q: QueryProtos.QueryRequest): QueryProtos.QueryResponse =
        stub.withDeadlineAfter(deadline, TimeUnit.SECONDS).sendQuery(q)

    private fun initialize() {
        fun isLocalPortFree(port: Int) =
            try {
                ServerSocket(port).close()
                true
            } catch (e: IOException) {
                false
            }

        var p = port
        while (!isLocalPortFree(p)) p++

        // Supports any way of defining ips and ports anywhere in the params. Now {ip}:{port} is just as valid as
        // -p={ip}:{port} and they can be written in any order with other params such as a new -t(threads) flag in Reveaal
        // or -i(input folder flag) in J-Ecdar
        val currentParams = Array(params.size) {
            params[it].replace("{ip}", ip).replace("{port}", p.toString())
        }

        proc =
            ProcessBuilder(path,
                *currentParams)
                // .redirectOutput(ProcessBuilder.Redirect.appendTo(File("Engine-$name-log.txt")))
                .redirectErrorStream(true) // wanted to just get outputstream, however outputstream is empty until the
                // process closes. So this would work fine on  "echo hello", but won't work on starting servers in JDK 11.
                // Somehow redirecting the error output fixes the input stream
                .directory(File(path).parentFile)
                .start()
        port = p

        val inputAndErrorStream = proc?.inputStream ?: throw Exception("Process exited unexpectedly")
        val reader = BufferedReader(InputStreamReader(inputAndErrorStream))
        var output: String? = null

        while (output == null) {
            output = reader.readLine()
        }
        println(output) // example: "Started grpc server on '127.0.0.1:70XX'
    }

    private fun resetStub() {
        proc!!.destroy()
        initialize()
    }

    fun terminate() {
        proc!!.destroy()
    }

    private fun componentUpdateFromPath(path: String): ComponentProtos.ComponentsInfo {
        val file = File(path)
        return if (File(path).isFile) { // If XML
            val component = ComponentProtos.Component.newBuilder().setXml(file.readText()).build()
            ComponentProtos.ComponentsInfo.newBuilder()
                .addComponents(component)
                .setComponentsHash(component.hashCode())
                .build()
        } else { // If json (e.g directory)
            val localFile = File(path.plus("/Components"))
            val components =
                localFile
                    .listFiles()!! // .filter { it.endsWith(".json") }
                    .map { ComponentProtos.Component.newBuilder().setJson(it.readText()).build() }
            ComponentProtos.ComponentsInfo.newBuilder()
                .addAllComponents(components)
                .setComponentsHash(components.hashCode())
                .build()
        }
    }
}
