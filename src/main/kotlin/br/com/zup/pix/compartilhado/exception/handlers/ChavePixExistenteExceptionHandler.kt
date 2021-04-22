package br.com.zup.pix.compartilhado.exception.handlers

import br.com.zup.pix.compartilhado.exception.ExceptionHandler
import br.com.zup.pix.compartilhado.exception.ExceptionHandler.StatusWithDetails
import br.com.zup.pix.compartilhado.exception.customizada.ChavePixExistenteException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixExistenteExceptionHandler : ExceptionHandler<ChavePixExistenteException> {

    override fun handle(e: ChavePixExistenteException): StatusWithDetails {
        return StatusWithDetails(
            Status.ALREADY_EXISTS
            .withDescription(e.message)
            .withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixExistenteException
    }
}
