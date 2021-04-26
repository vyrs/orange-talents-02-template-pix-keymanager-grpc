package br.com.zup.pix.busca

import br.com.zup.BuscaChavePixRequest
import br.com.zup.ContaEnum
import br.com.zup.PixBuscaGrpcServiceGrpc
import br.com.zup.PixRegistraGrpcServiceGrpc
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.TipoChave
import br.com.zup.pix.integracao.bcb.*
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
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class BuscaEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: PixBuscaGrpcServiceGrpc.PixBuscaGrpcServiceBlockingStub
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
    fun `deve retornar dados locais da chave somente passando a chave`() {
        // ação
        val response = grpcClient.busca(
            BuscaChavePixRequest.newBuilder()
                .setChave(CHAVE_EXISTENTE.chave)
                .build()
        )

        // validação
        with(response) {
            assertEquals(CHAVE_EXISTENTE.id.toString(), this.pixId)
            assertEquals(CHAVE_EXISTENTE.clienteId.toString(), this.clienteId)
            assertEquals(CHAVE_EXISTENTE.tipoChave.name, this.dadosChave.tipoChave.name)
            assertEquals(CHAVE_EXISTENTE.chave, this.dadosChave.chave)
        }

    }

    @Test
    fun `deve retornar dados da chave buscando no bcb somente passando a chave`() {
        // cenario
        `when`(bcbClient.findByKey("yuri@mail.com"))
            .thenReturn(HttpResponse.ok(buscaDetailsResponse()))

        // ação
        val response = grpcClient.busca(
            BuscaChavePixRequest.newBuilder()
                .setChave("yuri@mail.com")
                .build()
        )

        // validação
        with(response) {
            assertEquals("", this.pixId)
            assertEquals("", this.clienteId)
            assertEquals("EMAIL", this.dadosChave.tipoChave.name)
            assertEquals("yuri@mail.com", this.dadosChave.chave)
        }

    }

    @Test
    fun `deve retornar dados locais da chave buscando Por PixId`() {
        // ação
        val response = grpcClient.busca(
            BuscaChavePixRequest.newBuilder()
                .setPixId(
                    BuscaChavePixRequest.FiltroPorPixId.newBuilder()
                        .setPixId(CHAVE_EXISTENTE.id.toString())
                        .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                        .build()
                ).build()
        )

        // validação
        with(response) {
            assertEquals(CHAVE_EXISTENTE.id.toString(), this.pixId)
            assertEquals(CHAVE_EXISTENTE.clienteId.toString(), this.clienteId)
            assertEquals(CHAVE_EXISTENTE.tipoChave.name, this.dadosChave.tipoChave.name)
            assertEquals(CHAVE_EXISTENTE.chave, this.dadosChave.chave)
        }

    }

    @Test
    fun `nao deve retornar dados locais da chave buscando Por PixId quando chave não pertence ao cliente`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.busca(
            BuscaChavePixRequest.newBuilder()
                .setPixId(
                    BuscaChavePixRequest.FiltroPorPixId.newBuilder()
                        .setPixId(CHAVE_EXISTENTE.id.toString())
                        .setClienteId(UUID.randomUUID().toString())
                        .build()
                ).build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada ou não pertence a este Cliente!", status.description)
        }

    }

    @Test
    fun `nao deve retornar dados locais da chave buscando Por PixId quando chave não existe`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.busca(
            BuscaChavePixRequest.newBuilder()
                .setPixId(
                    BuscaChavePixRequest.FiltroPorPixId.newBuilder()
                        .setPixId(UUID.randomUUID().toString())
                        .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                        .build()
                ).build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada ou não pertence a este Cliente!", status.description)
        }

    }

    @Test
    fun `nao deve retornar dados locais da chave buscando Por chave quando chave não existe`() {
        // cenario
        val chaveInexistente = UUID.randomUUID().toString()

        `when`(bcbClient.findByKey(chaveInexistente))
            .thenReturn(HttpResponse.notFound())

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.busca(
            BuscaChavePixRequest.newBuilder()
                .setChave(chaveInexistente)
                .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada!", status.description)
        }

    }

    @Test
    fun `nao deve fazer a busca com os dados de entrada forem invalidos`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.busca(
            BuscaChavePixRequest.newBuilder().build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Chave Pix inválida ou não informada!", status.description)
        }

    }

    @Test
    fun `nao deve fazer a busca com os dados de entrada forem blank`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.busca(
            BuscaChavePixRequest.newBuilder().setChave("").build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos!", status.description)
        }

    }


    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient? {
        return Mockito.mock(BancoCentralClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixBuscaGrpcServiceGrpc.PixBuscaGrpcServiceBlockingStub? {
            return PixBuscaGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

private fun buscaDetailsResponse(): PixKeyDetailsResponse {
    return PixKeyDetailsResponse(
        keyType = PixKeyType.EMAIL,
        key = "yuri@mail.com",
        bankAccount = bankAccount(),
        owner = owner(),
        createdAt = LocalDateTime.now()
    )
}

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = "90400888",
            branch = "9871",
            accountNumber = "987654",
            accountType = BankAccount.AccountType.SVGS
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Vitor",
            taxIdNumber = "12345678901"
        )
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