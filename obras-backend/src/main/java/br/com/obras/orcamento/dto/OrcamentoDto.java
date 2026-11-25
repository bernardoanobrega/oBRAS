package br.com.obras.orcamento.dto;

import br.com.obras.orcamento.domain.Orcamento;
import br.com.obras.orcamento.domain.StatusOrcamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Painel orçamentário da obra (RF-74): planilha, histórico de decisões e indicadores. */
public record OrcamentoDto(Long id, Long obraId, StatusOrcamento status, LocalDate dataElaboracao,
                           LocalDate dataUltimaAtualizacao, BigDecimal percentualLimiteAlerta,
                           IndicadorSaudeFinanceiraDto indicadores, List<ItemOrcadoDto> itens,
                           List<DecisaoFinanceiraDto> decisoes) {
    public static OrcamentoDto de(Orcamento orcamento) {
        return new OrcamentoDto(orcamento.getId(), orcamento.getObraId(), orcamento.getStatus(),
                orcamento.getDataElaboracao(), orcamento.getDataUltimaAtualizacao(),
                orcamento.getPercentualLimiteAlerta(),
                IndicadorSaudeFinanceiraDto.de(orcamento.gerarIndicadoresSaude()),
                orcamento.getItens().stream().map(ItemOrcadoDto::de).toList(),
                orcamento.getDecisoes().stream().map(DecisaoFinanceiraDto::de).toList());
    }
}
