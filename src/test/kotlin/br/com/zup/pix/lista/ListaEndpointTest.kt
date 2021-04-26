package br.com.zup.pix.lista

import br.com.zup.ContaEnum
import br.com.zup.ListaChavesPixRequest
import br.com.zup.PixBuscaGrpcServiceGrpc
import br.com.zup.PixListaGrpcServiceGrpc
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.TipoChave
import br.com.zup.pix.model.ChavePix
import br.com.zup.pix.model.Conta
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class ListaEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: PixListaGrpcServiceGrpc.PixListaGrpcServiceBlockingStub
) {

    companion object {
        var clienteId = UUID.randomUUID()
        var clienteId2 = UUID.randomUUID()
    }
    lateinit var CHAVE_EXISTENTE1: ChavePix
    lateinit var CHAVE_EXISTENTE2: ChavePix
    lateinit var CHAVE_EXISTENTE3: ChavePix


    @BeforeEach
    fun setup() {
        repository.deleteAll()
        CHAVE_EXISTENTE1 = repository.save(chave(
            tipo = TipoChave.EMAIL,
            chave = "vitor@mail.com",
            clienteId = clienteId
        ))
        CHAVE_EXISTENTE2 = repository.save(chave(
            tipo = TipoChave.CPF,
            chave = "63657520325",
            clienteId = clienteId
        ))
        CHAVE_EXISTENTE3 = repository.save(chave(
            tipo = TipoChave.EMAIL,
            chave = "yuri@mail.com",
            clienteId = clienteId2
        ))
    }

    @Test
    fun `deve retornar uma lista de chaves do cliente`() {
        //ação
        val response = grpcClient.lista(
            ListaChavesPixRequest.newBuilder().setClienteId(clienteId.toString()).build()
        )

        // validação
        with(response) {
            assertThat(this.chavesList, hasSize(2))
            assertEquals(CHAVE_EXISTENTE1.clienteId.toString(), clienteId)
            assertEquals(CHAVE_EXISTENTE1.chave, chavesList.get(0).chave)
            assertEquals(CHAVE_EXISTENTE2.clienteId.toString(), clienteId)
            assertEquals(CHAVE_EXISTENTE2.chave, chavesList.get(1).chave)
        }
    }

    @Test
    fun `nao deve retornar uma lista de chaves do cliente quando ele nao tiver`() {
        //ação
        val response = grpcClient.lista(
            ListaChavesPixRequest.newBuilder().setClienteId(UUID.randomUUID().toString()).build()
        )

        // validação
        with(response) {
            assertThat(this.chavesList, hasSize(0))
        }
    }

    @Test
    fun `nao deve retornar uma lista de chaves do cliente quando clientId invalido`() {
        //ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.lista(
            ListaChavesPixRequest.newBuilder().setClienteId("").build())
        }


        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("O identificador do cliente é obrigatorio!", status.description)
        }
    }


    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixListaGrpcServiceGrpc.PixListaGrpcServiceBlockingStub? {
            return PixListaGrpcServiceGrpc.newBlockingStub(channel)
        }
    }


    private fun chave(
        tipo: TipoChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipoChave = tipo,
            chave = chave,
            tipoConta = ContaEnum.CONTA_CORRENTE,
            conta = Conta(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Rafael Ponte",
                cpfDoTitular = "63657520325",
                agencia = "1218",
                numeroDaConta = "291900"
            )
        )
    }
}