package br.com.zup.pix.busca

import br.com.zup.BuscaChavePixRequest
import br.com.zup.BuscaChavePixResponse
import br.com.zup.PixBuscaGrpcServiceGrpc
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.compartilhado.exception.ErrorHandler
import br.com.zup.pix.integracao.bcb.BancoCentralClient
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandler
@Singleton
class BuscaEndpoint(
    @Inject private val repository: ChavePixRepository,
    @Inject private val bcbClient: BancoCentralClient,
    @Inject private val validator: Validator
) : PixBuscaGrpcServiceGrpc.PixBuscaGrpcServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun busca(
        request: BuscaChavePixRequest,
        responseObserver: StreamObserver<BuscaChavePixResponse>) {

        logger.info("Buscando chave: $request")

        val chaveFiltro = request.toModel(validator)
        val chave = chaveFiltro.filtra(repository, bcbClient)

        responseObserver.onNext(BuscaChavePixResponseConverter().converte(chave))
        responseObserver.onCompleted()
    }

}