package br.com.obras.orcamento.dto;

import br.com.obras.orcamento.domain.IndicadorSaudeFinanceira;
import br.com.obras.orcamento.domain.SaudeFinanceira;

import java.math.BigDecimal;

public record IndicadorSaudeFinanceiraDto(BigDecimal totalOrcado, BigDecimal totalRealizado,
                                          BigDecimal desvioAbsoluto, BigDecimal desvioPercentual,
                                          BigDecimal percentualExecutado, int quantidadeItensEmEstouro,
                                          SaudeFinanceira saude, boolean possuiAlerta) {
    public static IndicadorSaudeFinanceiraDto de(IndicadorSaudeFinanceira indicador) {
        return new IndicadorSaudeFinanceiraDto(indicador.getTotalOrcado(), indicador.getTotalRealizado(),
                indicador.getDesvioAbsoluto(), indicador.getDesvioPercentual(), indicador.getPercentualExecutado(),
                indicador.getQuantidadeItensEmEstouro(), indicador.getSaude(), indicador.possuiAlerta());
    }
}
