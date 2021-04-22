package br.com.zup.pix.busca

import br.com.zup.ContaEnum
import br.com.zup.pix.TipoChave
import br.com.zup.pix.model.ChavePix
import br.com.zup.pix.model.Conta
import java.time.LocalDateTime
import java.util.*

data class ChavePixInfo(
    val pixId: UUID? = null,
    val clienteId: UUID? = null,
    val tipo: TipoChave,
    val chave: String,
    val tipoDeConta: ContaEnum,
    val conta: Conta,
    val registradaEm: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        fun of(chave: ChavePix): ChavePixInfo {
            return ChavePixInfo(
                pixId = chave.id,
                clienteId = chave.clienteId,
                tipo = chave.tipoChave,
                chave = chave.chave!!,
                tipoDeConta = chave.tipoConta,
                conta = chave.conta,
                registradaEm = chave.criadaEm
            )
        }
    }
}
