package br.com.zup.pix.busca

import br.com.zup.BuscaChavePixResponse
import br.com.zup.ChaveEnum
import br.com.zup.ContaEnum
import com.google.protobuf.Timestamp
import java.time.ZoneId

class BuscaChavePixResponseConverter {
    fun converte(chaveInfo: ChavePixInfo): BuscaChavePixResponse {
        return BuscaChavePixResponse.newBuilder()
            .setClienteId(chaveInfo.clienteId?.toString() ?: "")
            .setPixId(chaveInfo.pixId?.toString() ?: "")
            .setDadosChave(BuscaChavePixResponse.Chave
                .newBuilder()
                .setTipoChave(ChaveEnum.valueOf(chaveInfo.tipo.name))
                .setChave(chaveInfo.chave)
                .setTipoConta(ContaEnum.valueOf(chaveInfo.tipoDeConta.name))
                .setConta(BuscaChavePixResponse.Chave.Conta.newBuilder()
                    .setInstituicao(chaveInfo.conta.instituicao)
                    .setNomeTitular(chaveInfo.conta.nomeDoTitular)
                    .setCpfTitular(chaveInfo.conta.cpfDoTitular)
                    .setAgencia(chaveInfo.conta.agencia)
                    .setNumeroConta(chaveInfo.conta.numeroDaConta)
                    .build()
                )
                .setCriadaEm(chaveInfo.registradaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
            ).build()
    }
}
