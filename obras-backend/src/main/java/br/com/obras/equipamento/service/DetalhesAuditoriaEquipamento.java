package br.com.obras.equipamento.service;

import br.com.obras.equipamento.domain.StatusEquipamento;
import br.com.obras.equipamento.domain.TipoEquipamento;
import java.time.LocalDate;

/**
 * Monta o campo {@code detalhe} (JSON) do registro de auditoria. Só entram ids, enums e datas ISO,
 * nunca texto livre do usuário, para o JSON permanecer válido.
 */
final class DetalhesAuditoriaEquipamento {

    private DetalhesAuditoriaEquipamento() {
    }

    static String cadastro(TipoEquipamento tipo) {
        return String.format("{\"tipo\":\"%s\"}", tipo);
    }

    static String edicao() {
        return "{\"campos\":\"descricao,tipo\"}";
    }

    static String mudancaDeStatus(StatusEquipamento anterior, StatusEquipamento novo) {
        return String.format("{\"statusAnterior\":\"%s\",\"statusNovo\":\"%s\"}", anterior, novo);
    }

    static String alocacao(Long equipamentoId, Long obraId) {
        return String.format("{\"equipamentoId\":%d,\"obraId\":%d}", equipamentoId, obraId);
    }

    static String devolucao() {
        return "{\"acao\":\"DEVOLUCAO\"}";
    }

    static String transferencia(Long obraDestinoId) {
        return String.format("{\"acao\":\"TRANSFERENCIA\",\"obraDestinoId\":%d}", obraDestinoId);
    }

    static String prorrogacao(LocalDate novaDataPrevista) {
        return String.format("{\"acao\":\"PRORROGACAO\",\"novaDataPrevista\":\"%s\"}", novaDataPrevista);
    }
}