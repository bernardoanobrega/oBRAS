package br.com.obras.estoque;

import br.com.obras.estoque.domain.Material;
import br.com.obras.estoque.domain.SaldoEstoque;
import br.com.obras.estoque.domain.SituacaoSaldo;
import br.com.obras.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Cobre o diagrama de estados do SaldoEstoque: ESGOTADO, ALERTA_MINIMO e REGULAR. */
class SaldoEstoqueTest {
    private static BigDecimal n(String valor) { return new BigDecimal(valor); }

    private SaldoEstoque novoSaldo(String minima) {
        Material cimento = Material.cadastrar("Cimento CP-II", null, "saco");
        return SaldoEstoque.criar(cimento, 1L, n(minima));
    }

    @Test
    void comecaEsgotadoEEmAlerta() {
        SaldoEstoque saldo = novoSaldo("10");
        assertEquals(SituacaoSaldo.ESGOTADO, saldo.situacao());
        assertTrue(saldo.verificarAlertaMinimo());
    }

    @Test
    void esgotadoVaiParaAlertaMinimoOuRegularAoAdicionar() {
        SaldoEstoque emAlerta = novoSaldo("10");
        emAlerta.adicionarSaldo(n("10"));
        assertEquals(SituacaoSaldo.ALERTA_MINIMO, emAlerta.situacao());

        SaldoEstoque regular = novoSaldo("10");
        regular.adicionarSaldo(n("10.001"));
        assertEquals(SituacaoSaldo.REGULAR, regular.situacao());
        assertFalse(regular.verificarAlertaMinimo());
    }

    @Test
    void regularCaiParaAlertaMinimoESegueParaEsgotado() {
        SaldoEstoque saldo = novoSaldo("10");
        saldo.adicionarSaldo(n("30"));
        saldo.subtrairSaldo(n("15"));
        assertEquals(SituacaoSaldo.REGULAR, saldo.situacao());

        saldo.subtrairSaldo(n("5"));
        assertEquals(SituacaoSaldo.ALERTA_MINIMO, saldo.situacao());

        saldo.subtrairSaldo(n("10"));
        assertEquals(SituacaoSaldo.ESGOTADO, saldo.situacao());
    }

    @Test
    void naoSubtraiMaisQueOSaldoENadaMuda() {
        SaldoEstoque saldo = novoSaldo("0");
        saldo.adicionarSaldo(n("5"));
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> saldo.subtrairSaldo(n("5.001")));
        assertEquals("Quantidade insuficiente em estoque", erro.getMessage());
        assertEquals(0, n("5").compareTo(saldo.getQuantidadeAtual()));
    }

    @Test
    void rejeitaQuantidadeZeroOuNegativa() {
        SaldoEstoque saldo = novoSaldo("0");
        assertThrows(RegraNegocioException.class, () -> saldo.adicionarSaldo(BigDecimal.ZERO));
        assertThrows(RegraNegocioException.class, () -> saldo.subtrairSaldo(n("-1")));
    }

    @Test
    void somaDecimalEhExata() {
        SaldoEstoque saldo = novoSaldo("0");
        saldo.adicionarSaldo(n("0.1"));
        saldo.adicionarSaldo(n("0.2"));
        saldo.subtrairSaldo(n("0.3"));
        assertEquals(SituacaoSaldo.ESGOTADO, saldo.situacao());
    }

    @Test
    void alterarMinimaPodeMudarASituacao() {
        SaldoEstoque saldo = novoSaldo("10");
        saldo.adicionarSaldo(n("20"));
        assertEquals(SituacaoSaldo.REGULAR, saldo.situacao());
        saldo.definirQuantidadeMinima(n("25"));
        assertEquals(SituacaoSaldo.ALERTA_MINIMO, saldo.situacao());
    }

    @Test
    void minimaNegativaERejeitada() {
        assertThrows(RegraNegocioException.class, () -> novoSaldo("0").definirQuantidadeMinima(n("-1")));
    }
}
