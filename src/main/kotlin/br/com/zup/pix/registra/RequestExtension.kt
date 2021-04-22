package br.com.zup.pix.registra

import br.com.zup.ChaveEnum.TIPO_CHAVE_DESCONHECIDA
import br.com.zup.ContaEnum
import br.com.zup.ContaEnum.TIPO_CONTA_DESCONHECIDA
import br.com.zup.RegistraChavePixRequest
import br.com.zup.pix.TipoChave

fun RegistraChavePixRequest.toModel(): NovaChavePix {
    return NovaChavePix(
        clienteId = clienteId,
        chave =  chave,
        tipoConta = when (tipoConta) {
            TIPO_CONTA_DESCONHECIDA -> null
            else -> ContaEnum.valueOf(tipoConta.name)
        },
        tipoChave = when (tipoChave) {
            TIPO_CHAVE_DESCONHECIDA -> null
            else -> TipoChave.valueOf(tipoChave.name)
        }
    )
}