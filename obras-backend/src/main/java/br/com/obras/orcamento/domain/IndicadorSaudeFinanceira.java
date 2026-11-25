package br.com.obras.orcamento.domain;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * Value object calculado sob demanda (sem tabela). Regras de classificação (RF-34):
 * CRITICA quando o realizado passa do orçado; ATENCAO quando o executado atinge o limite de alerta
 * ou existe item em estouro; SAUDAVEL nos demais casos.
 */
@Getter
public final class IndicadorSaudeFinanceira {
    private final BigDecimal totalOrcado;
    private final BigDecimal totalRealizado;
    private final BigDecimal desvioAbsoluto;
    private final BigDecimal desvioPercentual;
    private final BigDecimal percentualExecutado;
    private final int quantidadeItensEmEstouro;
    private final SaudeFinanceira saude;

    public IndicadorSaudeFinanceira(BigDecimal totalOrcado, BigDecimal totalRealizado,
                                    int quantidadeItensEmEstouro, BigDecimal limiteAlerta) {
        this.totalOrcado = CalculoFinanceiro.monetario(totalOrcado);
        this.totalRealizado = CalculoFinanceiro.monetario(totalRealizado);
        this.desvioAbsoluto = this.totalRealizado.subtract(this.totalOrcado);
        this.desvioPercentual = CalculoFinanceiro.percentual(desvioAbsoluto, this.totalOrcado);
        this.percentualExecutado = CalculoFinanceiro.percentual(this.totalRealizado, this.totalOrcado);
        this.quantidadeItensEmEstouro = quantidadeItensEmEstouro;
        this.saude = classificarSaude(limiteAlerta);
    }

    public SaudeFinanceira classificarSaude(BigDecimal limiteAlerta) {
        boolean ultrapassouOrcado = totalRealizado.compareTo(totalOrcado) > 0;
        if (ultrapassouOrcado) {
            return SaudeFinanceira.CRITICA;
        }
        boolean atingiuLimite = CalculoFinanceiro.atingiuPercentual(totalRealizado, totalOrcado, limiteAlerta);
        if (atingiuLimite || quantidadeItensEmEstouro > 0) {
            return SaudeFinanceira.ATENCAO;
        }
        return SaudeFinanceira.SAUDAVEL;
    }

    public boolean possuiAlerta() {
        return saude != SaudeFinanceira.SAUDAVEL;
    }
}
