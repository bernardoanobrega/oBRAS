package br.com.obras.estoque;

import br.com.obras.estoque.domain.Material;
import br.com.obras.estoque.domain.MovimentacaoEstoque;
import br.com.obras.estoque.domain.TipoMovimentacao;
import br.com.obras.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MovimentacaoEstoqueTest {
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 14, 10, 0);
    private final Material material = Material.cadastrar("Areia", null, "m3");

    @Test
    void entradaPorCompraTemSoDestinoEPedido() {
        MovimentacaoEstoque m = MovimentacaoEstoque.entradaCompra(material, BigDecimal.TEN, 2L, 50L, 7L, AGORA);
        assertEquals(TipoMovimentacao.ENTRADA_COMPRA, m.getTipo());
        assertNull(m.getCanteiroOrigemId());
        assertEquals(2L, m.getCanteiroDestinoId());
        assertEquals(50L, m.getPedidoCompraId());
    }

    @Test
    void entradaSemPedidoERejeitada() {
        assertThrows(RegraNegocioException.class,
                () -> MovimentacaoEstoque.entradaCompra(material, BigDecimal.TEN, 2L, null, 7L, AGORA));
    }

    @Test
    void saidaTemSoOrigem() {
        MovimentacaoEstoque m = MovimentacaoEstoque.saidaConsumo(material, BigDecimal.ONE, 1L, 7L, AGORA);
        assertEquals(1L, m.getCanteiroOrigemId());
        assertNull(m.getCanteiroDestinoId());
        assertNull(m.getPedidoCompraId());
    }

    @Test
    void transferenciaExigeCanteirosDiferentes() {
        assertThrows(RegraNegocioException.class,
                () -> MovimentacaoEstoque.transferencia(material, BigDecimal.ONE, 1L, 1L, 7L, AGORA));
        MovimentacaoEstoque m = MovimentacaoEstoque.transferencia(material, BigDecimal.ONE, 1L, 2L, 7L, AGORA);
        assertEquals(1L, m.getCanteiroOrigemId());
        assertEquals(2L, m.getCanteiroDestinoId());
    }

    @Test
    void quantidadeZeroERejeitada() {
        assertThrows(RegraNegocioException.class,
                () -> MovimentacaoEstoque.saidaConsumo(material, BigDecimal.ZERO, 1L, 7L, AGORA));
    }
}
