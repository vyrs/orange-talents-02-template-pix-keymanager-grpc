package br.com.zup.pix.registra

import br.com.zup.ChaveEnum
import br.com.zup.ContaEnum
import br.com.zup.PixRegistraGrpcServiceGrpc
import br.com.zup.RegistraChavePixRequest
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.TipoChave
import br.com.zup.pix.integracao.bcb.*
import br.com.zup.pix.integracao.itau.ContaResponse
import br.com.zup.pix.integracao.itau.InstituicaoResponse
import br.com.zup.pix.integracao.itau.ItauClient
import br.com.zup.pix.integracao.itau.TitularResponse
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
internal class RegistraChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: PixRegistraGrpcServiceGrpc.PixRegistraGrpcServiceBlockingStub
) {
    @Inject
    lateinit var bcbClient: BancoCentralClient
    @Inject
    lateinit var itauClient: ItauClient;

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve registrar uma nova chave pix cpf`() {
        // cenário
        `when`(itauClient.buscaContaCliente(id = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcbClient.create(createPixKeyRequest()))
            .thenReturn(HttpResponse.created(createPixKeyResponse()))

        // ação
        val response = grpcClient.registra(RegistraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .setTipoChave(ChaveEnum.CPF)
            .setChave("63657520325")
            .setTipoConta(ContaEnum.CONTA_CORRENTE)
            .build())

        // validação
        with(response) {
            assertEquals(CLIENTE_ID.toString(), clienteId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `deve registrar uma nova chave pix celular`() {
        // cenário
        `when`(itauClient.buscaContaCliente(id = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcbClient.create(createPixKeyRequest(keyType = PixKeyType.PHONE, key = "+559999999999")))
            .thenReturn(HttpResponse.created(createPixKeyResponse(keyType = PixKeyType.PHONE, key = "+559999999999")))

        // ação
        val response = grpcClient.registra(RegistraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .setTipoChave(ChaveEnum.CELULAR)
            .setChave("+559999999999")
            .setTipoConta(ContaEnum.CONTA_CORRENTE)
            .build())

        // validação
        with(response) {
            assertEquals(CLIENTE_ID.toString(), clienteId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `deve registrar uma nova chave pix aleatoria`() {
        // cenário
        `when`(itauClient.buscaContaCliente(id = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcbClient.create(createPixKeyRequest(keyType = PixKeyType.RANDOM, key = "")))
            .thenReturn(HttpResponse.created(createPixKeyResponse(keyType = PixKeyType.RANDOM, key = CLIENTE_ID.toString())))

        // ação
        val response = grpcClient.registra(RegistraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .setTipoChave(ChaveEnum.ALEATORIA)
            .setChave("")
            .setTipoConta(ContaEnum.CONTA_CORRENTE)
            .build())

        // validação
        with(response) {
            assertEquals(CLIENTE_ID.toString(), clienteId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `deve registrar uma nova chave pix email`() {
        // cenário
        `when`(itauClient.buscaContaCliente(id = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcbClient.create(createPixKeyRequest(keyType = PixKeyType.EMAIL, key = "vitor@mail.com")))
            .thenReturn(HttpResponse.created(createPixKeyResponse(keyType = PixKeyType.EMAIL, key = "vitor@mail.com")))

        // ação
        val response = grpcClient.registra(RegistraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .setTipoChave(ChaveEnum.EMAIL)
            .setChave("vitor@mail.com")
            .setTipoConta(ContaEnum.CONTA_CORRENTE)
            .build())

        // validação
        with(response) {
            assertEquals(CLIENTE_ID.toString(), clienteId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando ela ja existe`() {
        // cenário
        repository.save(chave(
            tipo = TipoChave.CPF,
            chave = "17499522032",
            clienteId = CLIENTE_ID
        ))

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoChave(ChaveEnum.CPF)
                .setChave("17499522032")
                .setTipoConta(ContaEnum.CONTA_CORRENTE)
                .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix '17499522032' existente", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando nao encontrar conta do cliente`() {
        // cenário
        `when`(itauClient.buscaContaCliente(id = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoChave(ChaveEnum.EMAIL)
                .setChave("vitor@mail.com")
                .setTipoConta(ContaEnum.CONTA_CORRENTE)
                .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado no Itau", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar uma chave com cpf invalido`() {
        // cenário
        `when`(itauClient.buscaContaCliente(id = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setChave("123456789")
                    .setTipoChave(ChaveEnum.CPF)
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoConta(ContaEnum.CONTA_CORRENTE)
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(
                "Dados inválidos!",
                status.description
            )
        }
    }

    @Test
    fun `nao deve registrar chave pix com email invalido`() {
        // cenario
        `when`(itauClient.buscaContaCliente(id = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        // acao
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setChave("vitormail")
                    .setTipoChave(ChaveEnum.EMAIL)
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoConta(ContaEnum.CONTA_CORRENTE)
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(
                "Dados inválidos!",
                status.description
            )
        }
    }

    @Test
    fun `nao deve registrar chave pix com celular invalido`() {
        // cenario
        `when`(itauClient.buscaContaCliente(id = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        // acao
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setChave("789456")
                    .setTipoChave(ChaveEnum.CELULAR)
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoConta(ContaEnum.CONTA_CORRENTE)
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(
                "Dados inválidos!",
                status.description
            )
        }
    }

    @Test
    fun `nao deve permitir que o usuario informe a chave quando for aleatoria`() {
        // cenario
        `when`(itauClient.buscaContaCliente(id = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        // acao
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setChave("789456")
                    .setTipoChave(ChaveEnum.ALEATORIA)
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoConta(ContaEnum.CONTA_CORRENTE)
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(
                "Dados inválidos!",
                status.description
            )
        }
    }

    @Test
    fun `nao deve registrar chave pix quando nao for possivel registrar chave no BCB`() {
        // cenário
        `when`(itauClient.buscaContaCliente(id = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcbClient.create(createPixKeyRequest()))
            .thenReturn(HttpResponse.badRequest())

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoChave(ChaveEnum.CPF)
                .setChave("63657520325")
                .setTipoConta(ContaEnum.CONTA_CORRENTE)
                .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix com dados null ou vazios`() {
        // acao
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder().build())
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

    @MockBean(ItauClient::class)
    fun itauClient(): ItauClient? {
        return Mockito.mock(ItauClient::class.java)
    }

    @Factory
    class Clients  {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixRegistraGrpcServiceGrpc.PixRegistraGrpcServiceBlockingStub? {
            return PixRegistraGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun dadosDaContaResponse(): ContaResponse {
        return ContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", Conta.ITAU_UNIBANCO_ISPB),
            agencia = "1218",
            numero = "291900",
            titular = TitularResponse("Rafael Ponte", "63657520325")
        )
    }

    private fun createPixKeyRequest(
        keyType: PixKeyType = PixKeyType.CPF,
        key: String = "63657520325"): CriaChaveRequest {
        return CriaChaveRequest(
            keyType = keyType,
            key = key,
            bankAccount = bankAccount(),
            owner = owner()
        )
    }

    private fun createPixKeyResponse(
        keyType: PixKeyType = PixKeyType.CPF,
        key: String = "63657520325"): CriaChaveResponse {
        return CriaChaveResponse(
            keyType = keyType,
            key = key,
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = Conta.ITAU_UNIBANCO_ISPB,
            branch = "1218",
            accountNumber = "291900",
            accountType = BankAccount.AccountType.CACC
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Rafael Ponte",
            taxIdNumber = "63657520325"
        )
    }

    private fun chave(
        tipo: TipoChave,
        chave: String = "",
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