package br.com.zup.pix.deleta

import br.com.zup.DeletaChavePixRequest
import br.com.zup.DeletaChavePixResponse
import br.com.zup.PixDeletaGrpcServiceGrpc
import br.com.zup.pix.compartilhado.exception.ErrorHandler
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class DeletaEndpoint(@Inject private val service: DeletaService)
    :PixDeletaGrpcServiceGrpc.PixDeletaGrpcServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun deleta(
        request: DeletaChavePixRequest,
        responseObserver: StreamObserver<DeletaChavePixResponse>) {

        logger.info("Deletando chave: $request")

        service.deleta(clienteId = request.clientId, pixId = request.pixId)

      val response: DeletaChavePixResponse = DeletaChavePixResponse.newBuilder()
            .setClientId(request.clientId)
            .setPixId(request.pixId)
            .build()

        logger.info("chave: ${response.pixId} deletada!")

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}