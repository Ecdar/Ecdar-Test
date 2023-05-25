import EcdarProtoBuf.ComponentProtos
import EcdarProtoBuf.EcdarBackendGrpc
import EcdarProtoBuf.QueryProtos
import io.grpc.StatusRuntimeException
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import parsing.EngineConfiguration
import tests.MultiTest
import tests.SingleTest
import tests.Test
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import EcdarProtoBuf.QueryProtos.QueryResponse.ResultCase
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class Executor(//val engineConfig: EngineConfiguration,
               address: String,
               private val deadline: Long,
               path: String,
               ip: String,
               port: Int,
               expr: String,
               private val settings: QueryProtos.QueryRequest.Settings)
{
    private var queryId = AtomicInteger(0)
    private var proc: Process? = null
    private val stub = EcdarBackendGrpc.newBlockingStub(ManagedChannelBuilder.forTarget(address).usePlaintext().build())
    private var lock: AtomicBoolean = AtomicBoolean(false)
    init {
        proc = ProcessBuilder(
            path,
            expr.replace("{port}", port.toString())
                .replace("{ip}", ip)
        )
            //.redirectOutput(ProcessBuilder.Redirect.appendTo(File("Engine-$name-log.txt")))
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .directory(File(path).parentFile)
            .start()
    }


    fun runTest(test: Test): TestResult {
        try {
            if (test is MultiTest) {
                val resses = test.tests.map { runTest(it) }
                val res = test.getResult(resses)
                var time: Double? = 0.0
                resses.forEach {
                    if (it.time == null) {
                        time = null
                        return@forEach
                    } else {
                        time = time?.plus(it.time!!)
                    }
                }
                res.time = time
                return res
            } else if (test is SingleTest) {

                val queryId = queryId.getAndAdd(1)

                val success: ResultCase?

                lock()
                val start = System.currentTimeMillis()

                try {
                    val componentUpdate = componentUpdateFromPath(test.projectPath)
                    val query = QueryProtos.QueryRequest.newBuilder()
                            .setQueryId(queryId)
                            .setQuery(test.query)
                            .setComponentsInfo(componentUpdate)
                            .setSettings(settings)
                            .build()

                    val result = try {
                        stub.withDeadlineAfter(deadline, TimeUnit.SECONDS).sendQuery(query)
                    }
                    catch (e: StatusRuntimeException) {
                        if (e.status.code == Status.Code.UNAVAILABLE) {
                            resetStub()
                            println("Unavailable")
                            stub.withDeadlineAfter(deadline, TimeUnit.SECONDS).sendQuery(query)
                        } else {
                            println(e)
                            //throw e
                            stub.withDeadlineAfter(deadline, TimeUnit.SECONDS).sendQuery(query)
                        }
                    } catch (e: IOException) {
                        resetStub()
                        println(e)
                        stub.withDeadlineAfter(deadline, TimeUnit.SECONDS).sendQuery(query)
                    }

                    success = when (val r = result.resultCase) {
                        ResultCase.PARSING_ERROR ->
                            throw Exception("Query: ${test.query} in ${test.projectPath} lead to parsing-error: ${result.error}")
                        ResultCase.ERROR ->
                            throw Exception("Query: ${test.query} in ${test.projectPath} lead to error: ${result.error}")
                        ResultCase.RESULT_NOT_SET ->
                            throw Exception("Query: ${test.query} in ${test.projectPath} could not produce result. Error: ${result.error}")
                        else -> r
                    }
                }
                finally {unlock()}

                val res = test.getResult(success!!)
                res.time = (System.currentTimeMillis() - start).toDouble()
                return res
            }
        } catch (e: Throwable) {
            if (test is MultiTest)
                throw Exception("Did not except MultiTest to throw error $e")
            val r = TestResult(test.toSingleTest(), ResultType.EXCEPTION, ResultType.NON_EXCEPTION, listOf())
            r.exception = e.toString()
            return r
        }
        throw Exception("Cannot execute test of type ${test.javaClass.name}")
    }

    private fun lock() {
        while (lock.get()) {
            Thread.sleep(100)
        }
        lock.getAndSet(true)
    }

    private fun unlock() {
        lock.getAndSet(false)
    }

    private fun resetStub() {
        // TODO: DO
    }

    fun terminate() {
        lock()
        proc!!.destroy()
        unlock()
    }

    private fun destroy(proc: ProcessHandle) {
        proc.descendants().forEach{
            destroy(it)
        }
        //proc.destroy()
        while (proc.isAlive) {
            Thread.sleep(100)
        }
    }

    private fun componentUpdateFromPath(path: String): ComponentProtos.ComponentsInfo {
        val file = File(path)
        return if (File(path).isFile) { //If XML
            val component = ComponentProtos.Component.newBuilder().setXml(file.readText()).build()
            //  QueryProtos.ComponentsUpdateRequest.newBuilder().addComponents(component).build()
            ComponentProtos.ComponentsInfo.newBuilder().addComponents(component).setComponentsHash(component.hashCode()).build()
        } else { //If json (e.g directory)
            val localFile = File(path.plus("/Components"))
            val components = localFile.listFiles()!!//.filter { it.endsWith(".json") }
                .map { ComponentProtos.Component.newBuilder().setJson(it.readText()).build() }
            ComponentProtos.ComponentsInfo.newBuilder().addAllComponents(components).setComponentsHash(components.hashCode()).build()
        }
    }
}
