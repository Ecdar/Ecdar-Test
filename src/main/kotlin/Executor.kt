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
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock


class Executor(val engineConfig: EngineConfiguration) {

    init {
        engineConfig.initialize()
    }
    private val numProcesses = engineConfig.processes

    private val channels = engineConfig.addresses.map {
        ManagedChannelBuilder.forTarget(it).usePlaintext().build()
    }

    private val stubs = channels.map { EcdarBackendGrpc.newBlockingStub(it) }
    var queryId = AtomicInteger(0)
    var usedStubs = (0 until numProcesses).map { ReentrantLock() }

    fun runTest(test: Test, deadline: Long?): TestResult {
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

                val success: Boolean?

                while (!usedStubs[stubId].tryLock()) {
                    stubId += 1

                    //Sleep for a bit when we've tried all stubs
                    if (stubId >= numProcesses) {
                        Thread.sleep(100)
                        stubId = 0
                    }
                }
                val start = System.currentTimeMillis()

                try {
                    val componentUpdate = componentUpdateFromPath(test.projectPath)

                    try {
                        stubs[stubId].updateComponents(componentUpdate)
                    }
                    catch (e: StatusRuntimeException) {
                        if (e.status.code == Status.Code.UNAVAILABLE) {
                            resetStub(stubId)
                            stubs[stubId].withWaitForReady().updateComponents(componentUpdate)
                        } else {
                            throw e
                        }
                    }
                    catch (e: IOException) {
                        resetStub(stubId)
                        stubs[stubId].withWaitForReady().updateComponents(componentUpdate)
                    }

                    val query = QueryProtos.Query.newBuilder().setId(queryId).setQuery(test.query).build()
                    val result = stubs[stubId].withDeadlineAfter(deadline ?: 30, TimeUnit.SECONDS).sendQuery(query)


                    if (result.hasError()) {
                        throw Exception("Query: ${test.query} in ${test.projectPath} lead to error: ${result.error}")
                    }

                    success = (result.hasRefinement() && result.refinement.success)
                            || (result.hasConsistency() && result.consistency.success)
                            || (result.hasDeterminism() && result.determinism.success)
                }
                finally {
                    usedStubs[stubId].unlock()
                }

                val res = test.getResult(success!!)
                res.time = (System.currentTimeMillis() - start).toDouble()
                return res
            }
        } catch (e: Throwable) {
            if (test is MultiTest) {
                throw Exception("Did not except MultiTest to throw error $e")
            }
            val r = TestResult(test.toSingleTest(), ResultType.EXCEPTION, ResultType.NON_EXCEPTION, listOf())
            r.exception = e.toString()
            return r
        }
        throw Exception("Cannot execute test of type ${test.javaClass.name}")
    }

    private fun resetStub(stubId: Int) {
        engineConfig.reset(stubId)
    }

    private fun componentUpdateFromPath(path: String): QueryProtos.ComponentsUpdateRequest {
        val file = File(path)
        return if (File(path).isFile) { //If XML
            val component = ComponentProtos.Component.newBuilder().setXml(file.readText()).build()
            QueryProtos.ComponentsUpdateRequest.newBuilder().addComponents(component).build()
        } else { //If json (e.g directory)
            val file = File(path.plus("/Components"))
            val components = file.listFiles()!!//.filter { it.endsWith(".json") }
                .map { ComponentProtos.Component.newBuilder().setJson(it.readText()).build() }
            //throw Exception("Only ${components.size} comps")
            QueryProtos.ComponentsUpdateRequest.newBuilder().addAllComponents(components).build()
        }
    }

}