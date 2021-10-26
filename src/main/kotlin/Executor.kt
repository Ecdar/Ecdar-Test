import EcdarProtoBuf.ComponentProtos
import EcdarProtoBuf.EcdarBackendGrpc
import EcdarProtoBuf.QueryProtos
import io.grpc.ManagedChannelBuilder
import parsing.EngineConfiguration
import tests.Test
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock


class Executor(val engineConfig: EngineConfiguration) {
    private val numChannels = 2
    val ports = (0 until numChannels).map { "5${it.toString().padStart(3, '0')}" } //1000 - 100n

    private val channels = ports.map {
        ManagedChannelBuilder.forTarget("127.0.0.1:$it").usePlaintext().build() }
    private val stubs = channels.map {EcdarBackendGrpc.newBlockingStub(it)}
    var queryId = AtomicInteger(0)
    var usedStubs = (0 until numChannels).map{ ReentrantLock() }

    fun runTest(test: Test): TestResult {

        if(test.query.startsWith("consistency")) throw Exception("Cannot handle consistency")

        val queryId = queryId.getAndAdd(1)
        val stubId = queryId % numChannels

        var testResult: TestResult? = null

        while(!usedStubs[stubId].tryLock()){ }

        try {
            val componentUpdate = componentUpdateFromPath(test.projectPath)

            stubs[stubId].updateComponents(componentUpdate)

            val query = QueryProtos.Query.newBuilder().setId(queryId).setQuery(test.query).build()
            val result = stubs[stubId].sendQuery(query)

            if(result.hasError()) {
                throw Exception("Query: ${test.query} in ${test.projectPath} lead to error: ${result.error}")
            }

            val success = result.hasRefinement() && result.refinement.success
            testResult = TestResult(test, success, QueryResult(test.query, ""))
        }
        finally {
            usedStubs[stubId].unlock()
        }

        return testResult!!
    }

    private fun componentUpdateFromPath(path: String) : QueryProtos.ComponentsUpdateRequest {
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