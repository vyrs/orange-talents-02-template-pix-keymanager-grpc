package br.com.zup.pix.deleta

import br.com.zup.ContaEnum
import br.com.zup.DeletaChavePixRequest
import br.com.zup.PixDeletaGrpcServiceGrpc
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.TipoChave
import br.com.zup.pix.integracao.bcb.BancoCentralClient
import br.com.zup.pix.integracao.bcb.DeletePixKeyRequest
import br.com.zup.pix.integracao.bcb.DeletePixKeyResponse
import br.com.zup.pix.model.ChavePix
import br.com.zup.pix.model.Conta
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class DeletaEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: PixDeletaGrpcServiceGrpc.PixDeletaGrpcServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: BancoCentralClient

    lateinit var CHAVE_EXISTENTE: ChavePix

    @BeforeEach
    fun setup() {
        repository.deleteAll()
        CHAVE_EXISTENTE = repository.save(chave(
            tipo = TipoChave.EMAIL,
            chave = "vitor@mail.com",
            clienteId = UUID.randomUUID()
        ))
    }

    @Test
    fun `deve remover a chave pix existente`() {
        // cenário
        `when`(bcbClient.delete("vitor@mail.com", DeletePixKeyRequest("vitor@mail.com")))
            .thenReturn(
                HttpResponse.ok(DeletePixKeyResponse(key = "vitor@mail.com",
                participant = Conta.ITAU_UNIBANCO_ISPB,
                deletedAt = LocalDateTime.now()))
            )

        // ação
        val response = grpcClient.deleta(
            DeletaChavePixRequest.newBuilder()
            .setPixId(CHAVE_EXISTENTE.id.toString())
            .setClientId(CHAVE_EXISTENTE.clienteId.toString())
            .build())

        // validação
        with(response) {
            assertEquals(CHAVE_EXISTENTE.id.toString(), pixId)
            assertEquals(CHAVE_EXISTENTE.clienteId.toString(), clientId)
        }
    }

    @Test
    fun `nao deve remover chave pix quando chave inexistente`() {
        // cenário
        val pixIdInexistente = UUID.randomUUID().toString()

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.deleta(DeletaChavePixRequest.newBuilder()
                .setPixId(pixIdInexistente)
                .setClientId(CHAVE_EXISTENTE.clienteId.toString())
                .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Essa Chave não foi encontrada ou não pertence a este cliente!", status.description)
        }
    }

    @Test
    fun `nao deve remover chave pix quando ela pertence a outro cliente`() {
        // cenário
        val OUTRO_CLIENTE = UUID.randomUUID().toString()

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.deleta(
                DeletaChavePixRequest.newBuilder()
                    .setPixId(CHAVE_EXISTENTE.id.toString())
                    .setClientId(OUTRO_CLIENTE)
                    .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Essa Chave não foi encontrada ou não pertence a este cliente!", status.description)
        }

    }

    @Test
    fun `nao deve remover a chave pix quando tiver erro no bcb`() {
        // cenário
        `when`(bcbClient.delete("vitor@mail.com", DeletePixKeyRequest("vitor@mail.com")))
            .thenReturn(
                HttpResponse.badRequest()
            )

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.deleta(
                DeletaChavePixRequest.newBuilder()
                    .setPixId(CHAVE_EXISTENTE.id.toString())
                    .setClientId(CHAVE_EXISTENTE.clienteId.toString())
                    .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao remover chave Pix no Banco Central do Brasil (BCB)", status.description)
        }
    }

    @Test
    fun `nao deve remover a chave pix quando os dados forem null ou blank`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.deleta(
                DeletaChavePixRequest.newBuilder().build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos!", status.description)
        }
    }



    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient? {
        return mock(BancoCentralClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixDeletaGrpcServiceGrpc.PixDeletaGrpcServiceBlockingStub? {
            return PixDeletaGrpcServiceGrpc.newBlockingStub(channel)
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