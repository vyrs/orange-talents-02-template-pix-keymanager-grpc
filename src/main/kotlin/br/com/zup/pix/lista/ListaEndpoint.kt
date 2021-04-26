package br.com.zup.pix.lista

import br.com.zup.*
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.compartilhado.exception.ErrorHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListaEndpoint(
    @Inject private val repository: ChavePixRepository
): PixListaGrpcServiceGrpc.PixListaGrpcServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun lista(
        request: ListaChavesPixRequest,
        responseObserver: StreamObserver<ListaChavesPixResponse>) {

        if (request.clienteId.isNullOrBlank())
            throw IllegalArgumentException("O identificador do cliente é obrigatorio!")

        logger.info("Buscando chaves do cliente: $request")

        val chaves = repository.findAllByClienteId(UUID.fromString(request.clienteId)).map {
            ListaChavesPixResponse.Chave.newBuilder()
                .setPixId(it.id.toString())
                .setTipoChave(ChaveEnum.valueOf(it.tipoChave.name))
                .setChave(it.chave)
                .setTipoConta(ContaEnum.valueOf(it.tipoConta.name))
                .setCriadaEm(it.criadaEm.let {
                    val criadaEm = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(criadaEm.epochSecond)
                        .setNanos(criadaEm.nano)
                        .build()
                })
                .build()
        }

        val resposta = ListaChavesPixResponse.newBuilder()
            .setClienteId(request.clienteId)
            .addAllChaves(chaves)
            .build()


        responseObserver.onNext(resposta)
        responseObserver.onCompleted()
    }
}