import EcdarProtoBuf.ComponentProtos
import EcdarProtoBuf.EcdarBackendGrpc
import EcdarProtoBuf.QueryProtos
import EcdarProtoBuf.QueryProtos.QueryResponse.ResultCase
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import io.grpc.StatusRuntimeException
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import parsing.EngineConfiguration
import tests.MultiTest
import tests.SingleTest
import tests.Test

class Executor(val engineConfig: EngineConfiguration) {

    init {
        engineConfig.initialize()
    }

    private val numProcesses = engineConfig.processes

    private val channels =
        engineConfig.addresses.map { ManagedChannelBuilder.forTarget(it).usePlaintext().build() }

    private val stubs = channels.map { EcdarBackendGrpc.newBlockingStub(it) }
    var queryId = AtomicInteger(0)
    private var usedStubs = (0 until numProcesses).map { ReentrantLock() }

    fun runTest(test: Test, deadline: Long): TestResult {
        try {
            if (test is MultiTest) {
                val resses = test.tests.map { runTest(it, deadline) }
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
                var stubId = 0

                val success: ResultCase?

                while (!usedStubs[stubId].tryLock()) {
                    stubId += 1

                    // Sleep for a bit when we've tried all stubs
                    if (stubId >= numProcesses) {
                        Thread.sleep(100)
                        stubId = 0
                    }
                }
                val start = System.currentTimeMillis()

                try {
                    val componentUpdate = componentUpdateFromPath(test.projectPath)
                    val settings = engineConfig.settings
                    val query =
                        QueryProtos.QueryRequest.newBuilder()
                            .setQueryId(queryId)
                            .setQuery(test.query)
                            .setComponentsInfo(componentUpdate)
                            .setSettings(settings)
                            .build()

                    val result =
                        try {
                            stubs[stubId]
                                .withDeadlineAfter(deadline, TimeUnit.SECONDS)
                                .sendQuery(query)
                        } catch (e: StatusRuntimeException) {
                            if (e.status.code == Status.Code.UNAVAILABLE) {
                                resetStub(stubId)
                                stubs[stubId]
                                    .withDeadlineAfter(deadline, TimeUnit.SECONDS)
                                    .sendQuery(query)
                            } else {
                                throw e
                            }
                        } catch (e: IOException) {
                            resetStub(stubId)
                            stubs[stubId]
                                .withDeadlineAfter(deadline, TimeUnit.SECONDS)
                                .sendQuery(query)
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
                } finally {
                    usedStubs[stubId].unlock()
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

    private fun resetStub(stubId: Int) {
        engineConfig.reset(stubId)
    }

    private fun componentUpdateFromPath(path: String): ComponentProtos.ComponentsInfo {
        val file = File(path)
        return if (File(path).isFile) { // If XML
            val component = ComponentProtos.Component.newBuilder().setXml(file.readText()).build()
            //  QueryProtos.ComponentsUpdateRequest.newBuilder().addComponents(component).build()
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
