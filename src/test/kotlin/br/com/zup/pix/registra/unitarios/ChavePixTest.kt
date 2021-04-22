package br.com.zup.pix.registra.unitarios

import br.com.zup.ContaEnum
import br.com.zup.pix.TipoChave
import br.com.zup.pix.model.ChavePix
import br.com.zup.pix.model.Conta
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class ChavePixTest {

    companion object {
        val TIPOS_DE_CHAVES_EXCETO_ALEATORIO = TipoChave.values().filterNot { it == TipoChave.ALEATORIA }
    }

    @Test
    fun deveChavePertencerAoCliente() {

        val clienteId = UUID.randomUUID()
        val outroClienteId = UUID.randomUUID()

        with (newChave(tipo = TipoChave.ALEATORIA, clienteId = clienteId)) {
            assertTrue(this.pertenceAo(clienteId))
            assertFalse(this.pertenceAo(outroClienteId))
        }
    }

    @Test
    fun deveChaveSerDoTipoAleatoria() {
        with (newChave(TipoChave.ALEATORIA)) {
            assertTrue(this.tipoChave == TipoChave.ALEATORIA)
        }
    }

    @Test
    fun naoDeveChaveSerDoTipoAleatoria() {
        TIPOS_DE_CHAVES_EXCETO_ALEATORIO
            .forEach {
                assertFalse(it == TipoChave.ALEATORIA)
            }
    }

    @Test
    fun deveAtualizarChaveQuandoForAleatoria() {
        with (newChave(TipoChave.ALEATORIA)) {
            assertTrue(this.atualiza("nova-chave"))
            assertEquals("nova-chave", this.chave)
        }
    }

    @Test
    fun naoDeveAtualizarChaveQuandoNaoForAleatoria() {

        val original = "chavealeatoria"

        TIPOS_DE_CHAVES_EXCETO_ALEATORIO
            .forEach {
                with (newChave(tipo = it, chave = original)) {
                    assertFalse(this.atualiza("nova-chave"))
                    assertEquals(original, this.chave)
                }
            }
    }

    private fun newChave(
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
                cpfDoTitular = "12345678900",
                agencia = "1218",
                numeroDaConta = "123456"
            )
        )
    }

}