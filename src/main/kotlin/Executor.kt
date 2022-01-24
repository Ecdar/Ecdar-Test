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

    fun runTest(test: Test): TestResult {
        if (test is MultiTest) {
            return test.getResult(test.tests.map { runTest(it) })
        } else if (test is SingleTest) {

            val queryId = queryId.getAndAdd(1)
            var stubId = 0

            val success: Boolean?

            while (!usedStubs[stubId].tryLock()) {
                stubId = (stubId + 1) % numProcesses
            }

            try {
                val componentUpdate = componentUpdateFromPath(test.projectPath)

                try {
                    stubs[stubId].updateComponents(componentUpdate)
                }
                catch (e: StatusRuntimeException) {
                    if (e.status.code == Status.Code.UNAVAILABLE) {
                        engineConfig.reset(stubId);
                        stubs[stubId].updateComponents(componentUpdate)
                    }
                }

                val query = QueryProtos.Query.newBuilder().setId(queryId).setQuery(test.query).build()
                val result = stubs[stubId].withDeadlineAfter(30, TimeUnit.SECONDS).sendQuery(query)

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

            return test.getResult(success!!)
        }
        throw Exception("Cannot execute test of type ${test.javaClass.name}")
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