package br.com.obras.orcamento.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Regras de arredondamento compartilhadas pelas entidades do orçamento. */
final class CalculoFinanceiro {
    static final BigDecimal CEM = new BigDecimal("100");
    static final int ESCALA_MONETARIA = 2;
    static final int ESCALA_PERCENTUAL = 2;
    static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_UP;

    private CalculoFinanceiro() {
    }

    static BigDecimal monetario(BigDecimal valor) {
        return valor.setScale(ESCALA_MONETARIA, ARREDONDAMENTO);
    }

    /** Percentual de parte sobre total. Total zero devolve zero para não dividir por zero. */
    static BigDecimal percentual(BigDecimal parte, BigDecimal total) {
        if (total.signum() == 0) {
            return BigDecimal.ZERO.setScale(ESCALA_PERCENTUAL);
        }
        return parte.multiply(CEM).divide(total, ESCALA_PERCENTUAL, ARREDONDAMENTO);
    }

    /** Compara sem arredondar: 899,99 de 1.000 (89,999%) não atinge o limite de 90%. */
    static boolean atingiuPercentual(BigDecimal parte, BigDecimal total, BigDecimal limitePercentual) {
        if (total.signum() <= 0) {
            return false;
        }
        return parte.multiply(CEM).compareTo(limitePercentual.multiply(total)) >= 0;
    }
}
