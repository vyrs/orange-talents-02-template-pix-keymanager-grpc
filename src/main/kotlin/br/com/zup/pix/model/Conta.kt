package br.com.zup.pix.model

import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Embeddable
class Conta(
    @field:NotBlank
    @Column(nullable = false)
    val instituicao: String,

    @field:NotBlank
    @Column(nullable = false)
    val nomeDoTitular: String,

    @field:NotBlank
    @field:Size(max = 11)
    @Column(length = 11, nullable = false)
    val cpfDoTitular: String,

    @field:NotBlank
    @field:Size(max = 4)
    @Column(length = 4, nullable = false)
    val agencia: String,

    @field:NotBlank
    @field:Size(max = 6)
    @Column(name = "conta_numero", length = 6, nullable = false)
    val numeroDaConta: String
) {

    companion object {
        public val ITAU_UNIBANCO_ISPB: String = "60701190"
    }
}
