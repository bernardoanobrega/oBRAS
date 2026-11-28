package br.com.obras.equipamento.dto;

import br.com.obras.equipamento.domain.AlocacaoEquipamento;
import java.time.LocalDate;

public record AlocacaoEquipamentoDto(
        Long id,
        Long equipamentoId,
        String codigoPatrimonio,
        Long obraId,
        Long responsavelId,
        LocalDate dataInicio,
        LocalDate dataPrevistaDevolucao,
        LocalDate dataFim,
        String observacao,
        String observacaoDevolucao,
        boolean estaAtiva,
        boolean estaAtrasada) {

    public static AlocacaoEquipamentoDto de(AlocacaoEquipamento alocacao) {
        return new AlocacaoEquipamentoDto(
                alocacao.getId(),
                alocacao.getEquipamento().getId(),
                alocacao.getEquipamento().getCodigoPatrimonio(),
                alocacao.getObraId(),
                alocacao.getResponsavelId(),
                alocacao.getDataInicio(),
                alocacao.getDataPrevistaDevolucao(),
                alocacao.getDataFim(),
                alocacao.getObservacao(),
                alocacao.getObservacaoDevolucao(),
                alocacao.estaAtiva(),
                alocacao.estaAtrasada());
    }
}