package br.com.zup.pix.registra

import br.com.zup.PixRegistraGrpcServiceGrpc
import br.com.zup.RegistraChavePixRequest
import br.com.zup.RegistraChavePixResponse
import br.com.zup.pix.compartilhado.exception.ErrorHandler
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@ErrorHandler
@Singleton
class RegistraChaveEndpoint(@Inject private val service: NovaChavePixService)
    : PixRegistraGrpcServiceGrpc.PixRegistraGrpcServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun registra(
        request: RegistraChavePixRequest,
        responseObserver: StreamObserver<RegistraChavePixResponse>
    ) {

        logger.info("Registrando chave: $request")


            val novaChave = request.toModel()
            val chaveCriada = service.registra(novaChave)

            var response = RegistraChavePixResponse.newBuilder()
                .setClienteId(chaveCriada.clienteId.toString())
                .setPixId(chaveCriada.id.toString())
                .build()


        logger.info("Chave ${response.pixId} criada!")

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

}