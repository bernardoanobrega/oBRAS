package br.com.obras.orcamento.dto;

import br.com.obras.orcamento.domain.Orcamento;
import br.com.obras.orcamento.domain.StatusOrcamento;

import java.time.LocalDate;

public record OrcamentoResumoDto(Long id, Long obraId, StatusOrcamento status, LocalDate dataUltimaAtualizacao) {
    public static OrcamentoResumoDto de(Orcamento orcamento) {
        return new OrcamentoResumoDto(orcamento.getId(), orcamento.getObraId(), orcamento.getStatus(),
                orcamento.getDataUltimaAtualizacao());
    }
}
