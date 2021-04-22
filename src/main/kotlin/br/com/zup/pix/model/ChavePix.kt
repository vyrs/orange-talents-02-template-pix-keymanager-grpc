package br.com.zup.pix.model

import br.com.zup.ContaEnum
import br.com.zup.pix.TipoChave
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChavePix(
    @field:NotNull
    @Column(nullable = false)
    val clienteId: UUID,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoChave: TipoChave,

    @field:NotNull
    @Column(unique = true)
    var chave: String?,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoConta: ContaEnum,

    @field:Valid
    @Embedded
    val conta: Conta
) {
    @Id
    @GeneratedValue
    val id: UUID? = null

    @Column(nullable = false)
    val criadaEm: LocalDateTime = LocalDateTime.now()

    override fun toString(): String {
        return "ChavePix(clienteId=$clienteId, tipo=$tipoChave, chave='$chave', tipoDeConta=$tipoConta, conta=$conta, id=$id, criadaEm=$criadaEm)"
    }

    /**
     * Verifica se esta chave pertence a este cliente
     */
    fun pertenceAo(clienteId: UUID) = this.clienteId.equals(clienteId)


    fun atualiza(chave: String): Boolean {
        if (this.tipoChave == TipoChave.ALEATORIA) { // somente a chave aleatoria pode ser alterada
            this.chave = chave
            return true
        }
        return false
    }

}
