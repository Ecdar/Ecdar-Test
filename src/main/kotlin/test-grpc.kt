import EcdarProtoBuf.ComponentProtos
import EcdarProtoBuf.EcdarBackendGrpc
import EcdarProtoBuf.QueryProtos
import io.grpc.ManagedChannelBuilder
import java.io.File

fun main() {
    val channel = ManagedChannelBuilder.forTarget("127.0.0.1:1000") // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
        // needing certificates.
        .usePlaintext()
        .build()

    val stub = EcdarBackendGrpc.newBlockingStub(channel)

    val component = ComponentProtos.Component.newBuilder().setJson(File("samples/json/AG/Components/A.json").readText()).build()
    val components = QueryProtos.ComponentsUpdateRequest.newBuilder().addComponents(component).build()
    stub.updateComponents(components)

    val query = QueryProtos.Query.newBuilder().setId(0).setQuery("refinement: A<=A").build()

    val response = stub.sendQuery(query)
    println("Refinement is ${response.refinement.success}")
}
