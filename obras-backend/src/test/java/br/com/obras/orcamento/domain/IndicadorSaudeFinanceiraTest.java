package br.com.obras.orcamento.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndicadorSaudeFinanceiraTest {
    private static final BigDecimal LIMITE_ALERTA = new BigDecimal("90");

    private IndicadorSaudeFinanceira indicador(String orcado, String realizado, int itensEmEstouro) {
        return new IndicadorSaudeFinanceira(new BigDecimal(orcado), new BigDecimal(realizado), itensEmEstouro,
                LIMITE_ALERTA);
    }

    @Test
    void abaixoDoLimiteSemEstouroESaudavel() {
        IndicadorSaudeFinanceira saudavel = indicador("1000", "899.99", 0);

        assertEquals(SaudeFinanceira.SAUDAVEL, saudavel.getSaude());
        assertFalse(saudavel.possuiAlerta());
    }

    @Test
    void atingirLimiteGeraAtencao() {
        assertEquals(SaudeFinanceira.ATENCAO, indicador("1000", "900", 0).getSaude());
    }

    @Test
    void itemEmEstouroGeraAtencaoMesmoAbaixoDoLimite() {
        IndicadorSaudeFinanceira atencao = indicador("1000", "100", 1);

        assertEquals(SaudeFinanceira.ATENCAO, atencao.getSaude());
        assertTrue(atencao.possuiAlerta());
    }

    @Test
    void realizadoAcimaDoOrcadoECritica() {
        assertEquals(SaudeFinanceira.CRITICA, indicador("1000", "1000.01", 1).getSaude());
    }

    @Test
    void orcadoZeroSemGastoESaudavel() {
        IndicadorSaudeFinanceira vazio = indicador("0", "0", 0);

        assertEquals(SaudeFinanceira.SAUDAVEL, vazio.getSaude());
        assertEquals(new BigDecimal("0.00"), vazio.getPercentualExecutado());
    }

    @Test
    void calculaDesvioPercentualSobreOrcado() {
        IndicadorSaudeFinanceira critico = indicador("4820000", "4887000", 2);

        assertEquals(new BigDecimal("67000.00"), critico.getDesvioAbsoluto());
        assertEquals(new BigDecimal("1.39"), critico.getDesvioPercentual());
    }
}
