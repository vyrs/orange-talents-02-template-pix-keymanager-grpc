package br.com.zup.pix.registra

import br.com.zup.ContaEnum
import br.com.zup.pix.TipoChave
import br.com.zup.pix.compartilhado.validacao.ValidaChavePix
import br.com.zup.pix.compartilhado.validacao.ValidaUUID
import br.com.zup.pix.model.ChavePix
import br.com.zup.pix.model.Conta
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidaChavePix
@Introspected
data class NovaChavePix(
    @field:ValidaUUID
    @field:NotBlank
    val clienteId: String?,

    @field:Size(max = 77)
    val chave: String?,

    @field:NotNull
    val tipoConta: ContaEnum?,

    @field:NotNull
    val tipoChave: TipoChave?
) {

    fun toModel(conta: Conta): ChavePix {
        return ChavePix(
            clienteId = UUID.fromString(this.clienteId),
            tipoChave = TipoChave.valueOf(this.tipoChave!!.name),
            chave = if (this.tipoChave == TipoChave.ALEATORIA) "" else this.chave!!,
            tipoConta = ContaEnum.valueOf(this.tipoConta!!.name),
            conta = conta
        )
    }

}