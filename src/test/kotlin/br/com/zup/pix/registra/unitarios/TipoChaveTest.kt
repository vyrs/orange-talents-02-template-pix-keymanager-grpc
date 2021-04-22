package br.com.zup.pix.registra.unitarios

import br.com.zup.pix.TipoChave
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TipoChaveTest {

    @Nested
    inner class ALEATORIA {

        @Test
        fun `deve ser valido o tipo aleatoria quando chave informada for nula ou vazia`() {
            with(TipoChave.ALEATORIA) {
                assertTrue(valida(null))
                assertTrue(valida(""))
            }
        }

        @Test
        fun `nao deve ser valido o tipo aleatoria quando chave informada possuir um valor`() {
            with(TipoChave.ALEATORIA) {
                assertFalse(valida("valor"))
            }
        }
    }

    @Nested
    inner class CPF {

        @Test
        fun `deve ser valido quando cpf for um numero valido`() {
            with(TipoChave.CPF) {
                assertTrue(valida("35060731332"))
            }
        }

        @Test
        fun `nao deve ser valido quando cpf for um numero invalido`() {
            with(TipoChave.CPF) {
                assertFalse(valida("35060731331"))
            }
        }

        @Test
        fun `nao deve ser valido quando cpf nao for informado`() {
            with(TipoChave.CPF) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }

        @Test
        fun `nao deve ser valido quando cpf possuir letras`() {
            with(TipoChave.CPF) {
                assertFalse(valida("3506073133a"))
            }
        }
    }

    @Nested
    inner class CELULAR {

        @Test
        fun `deve ser valido quando celular for um numero valido`() {
            with(TipoChave.CELULAR) {
                assertTrue(valida("+5511987654321"))
            }
        }

        @Test
        fun `nao deve ser valido quando celular for um numero invalido`() {
            with(TipoChave.CELULAR) {
                assertFalse(valida("11987654321"))
                assertFalse(valida("+55a11987654321"))
                assertFalse(valida("5511987"))
            }
        }

        @Test
        fun `nao deve ser valido quando celular nao for informado`() {
            with(TipoChave.CELULAR) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }
    }

    @Nested
    inner class EMAIL {

        @Test
        fun `deve ser valido quando email for endereco valido`() {
            with(TipoChave.EMAIL) {
                assertTrue(valida("vitor@mail.com.br"))
            }
        }

        @Test
        fun `nao deve ser valido quando email for invalido`() {
            with(TipoChave.EMAIL) {
                assertFalse(valida("vitor.com.br"))
                assertFalse(valida("vitor@mail.com."))
            }
        }

        @Test
        fun `nao deve ser valido quando email nao for informado`() {
            with(TipoChave.EMAIL) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }
    }
}