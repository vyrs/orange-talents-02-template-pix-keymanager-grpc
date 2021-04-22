package br.com.zup.pix.integracao.itau

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itau.url}")
interface ItauClient {

    @Get("/clientes/{id}/contas{?tipo}")
    fun buscaContaCliente(@PathVariable id: String, @QueryValue tipo: String): HttpResponse<ContaResponse>

}