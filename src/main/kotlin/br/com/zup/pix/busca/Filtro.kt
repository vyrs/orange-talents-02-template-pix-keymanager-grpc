package br.com.zup.pix.busca;

import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.compartilhado.exception.customizada.ChavePixNaoEncontradaException
import br.com.zup.pix.compartilhado.validacao.ValidaUUID
import br.com.zup.pix.integracao.bcb.BancoCentralClient
import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpStatus;
import org.slf4j.LoggerFactory
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {

    /**
     * Deve retornar chave encontrada ou lançar um exceção de erro de chave não encontrada
     */
    abstract fun filtra(repository: ChavePixRepository, bcbClient: BancoCentralClient): ChavePixInfo

    @Introspected
    data class PorPixId(
        @field:NotBlank @field:ValidaUUID val clienteId: String,
        @field:NotBlank @field:ValidaUUID val pixId: String,
            ) : Filtro() {

        fun pixIdAsUuid() = UUID.fromString(pixId)
        fun clienteIdAsUuid() = UUID.fromString(clienteId)

        override fun filtra(repository: ChavePixRepository, bcbClient: BancoCentralClient): ChavePixInfo {
            return repository.findById(pixIdAsUuid())
                    .filter { it.pertenceAo(clienteIdAsUuid()) }
                             .map(ChavePixInfo::of)
                    .orElseThrow { ChavePixNaoEncontradaException("Chave Pix não encontrada ou não pertence a este Cliente!") }
        }
    }

    @Introspected
    data class PorChave(@field:NotBlank @Size(max = 77) val chave: String) : Filtro() {

        private val logger = LoggerFactory.getLogger(this::class.java)

        override fun filtra(repository: ChavePixRepository, bcbClient: BancoCentralClient): ChavePixInfo {
            return repository.findByChave(chave)
                    .map(ChavePixInfo::of)
                    .orElseGet {
                        logger.info("Consultando chave Pix '$chave' no Banco Central do Brasil (BCB)")

                val response = bcbClient.findByKey(chave)
                when (response.status) {
                    HttpStatus.OK -> response.body()?.toModel()
                            else -> throw ChavePixNaoEncontradaException("Chave Pix não encontrada!")
                }
            }
        }
    }

    @Introspected
    class Invalido(): Filtro() {
        override fun filtra(repository: ChavePixRepository, bcbClient: BancoCentralClient): ChavePixInfo {
            throw IllegalArgumentException("Chave Pix inválida ou não informada!")
        }
    }
}
