package br.com.zup.pix.deleta

import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.compartilhado.exception.customizada.ChavePixNaoEncontradaException
import br.com.zup.pix.compartilhado.validacao.ValidaUUID
import br.com.zup.pix.integracao.bcb.BancoCentralClient
import br.com.zup.pix.integracao.bcb.DeletePixKeyRequest
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class DeletaService(@Inject val repository: ChavePixRepository,
                    @Inject val bcbClient: BancoCentralClient
) {

    @Transactional
    fun deleta(@NotBlank @ValidaUUID clienteId: String?,
                @NotBlank @ValidaUUID pixId: String?) {

        val pixIdUuid = UUID.fromString(pixId)
        val clienteIdUuid = UUID.fromString(clienteId)

        val chave = repository.findByIdAndClienteId(pixIdUuid, clienteIdUuid)
            .orElseThrow{
                ChavePixNaoEncontradaException("Essa Chave não foi encontrada ou não pertence a este cliente!")
            }

        val request = DeletePixKeyRequest(chave.chave!!)

        val bcbResponse = bcbClient.delete(key = chave.chave!!, request = request)
        if (bcbResponse.status != HttpStatus.OK) { // 1
            throw IllegalStateException("Erro ao remover chave Pix no Banco Central do Brasil (BCB)")
        }

        repository.deleteById(pixIdUuid)
    }
}
