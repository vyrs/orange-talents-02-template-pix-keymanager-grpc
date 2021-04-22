package br.com.zup.pix

import io.micronaut.validation.validator.constraints.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class TipoChave {
    CPF {
        override fun valida(chave: String?): Boolean {
            return when {
                chave.isNullOrBlank() -> false

                !chave.matches("[0-9]+".toRegex()) -> false

                else -> CPFValidator().run {
                    initialize(null)
                    isValid(chave, null)
                }
            }

        }
    },
    CELULAR {
        override fun valida(chave: String?): Boolean {
            return when {
                chave.isNullOrBlank() -> false
                else ->
                    chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
            }
        }
    },
    EMAIL {
        override fun valida(chave: String?): Boolean {
            return when {
                chave.isNullOrBlank() -> false
                else -> EmailValidator().run {
                    initialize(null)
                    isValid(chave, null)
                }
            }
        }
    },
    ALEATORIA {
        override fun valida(chave: String?) = chave.isNullOrBlank() // não deve se preenchida pelo usuario
    };

    abstract fun valida(chave: String?): Boolean

}