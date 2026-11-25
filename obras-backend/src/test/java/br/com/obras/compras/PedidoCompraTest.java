package br.com.obras.compras;

import br.com.obras.compras.domain.Cotacao;
import br.com.obras.compras.domain.Fornecedor;
import br.com.obras.compras.domain.PedidoCompra;
import br.com.obras.compras.domain.PrioridadePedido;
import br.com.obras.compras.domain.StatusPedido;
import br.com.obras.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Cobre o diagrama de estados do PedidoCompra: transições válidas e inválidas. */
class PedidoCompraTest {
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 14, 10, 0);
    private static final Long AUTOR = 7L;

    private PedidoCompra novoPedido() {
        return PedidoCompra.criar(1L, AUTOR, PrioridadePedido.MEDIA, null, AGORA);
    }

    private Fornecedor fornecedor() {
        return Fornecedor.cadastrar("ACME Ltda", "12.345.678/0001-99", "contato@acme.com", "79999999999");
    }

    private PedidoCompra pedidoEmCotacaoComItem() {
        PedidoCompra pedido = novoPedido();
        pedido.adicionarItem(10L, new BigDecimal("2.500"), new BigDecimal("10.00"), null);
        pedido.alterarStatus(StatusPedido.EM_COTACAO, AUTOR, AGORA);
        return pedido;
    }

    private PedidoCompra pedidoComprado() {
        PedidoCompra pedido = pedidoEmCotacaoComItem();
        Cotacao cotacao = pedido.registrarCotacao(fornecedor(), new BigDecimal("24.00"), 5, AGORA);
        ReflectionTestUtils.setField(cotacao, "id", 99L);
        pedido.selecionarCotacao(99L);
        pedido.alterarStatus(StatusPedido.COMPRADO, AUTOR, AGORA);
        return pedido;
    }

    @Test
    void nasceEmSolicitadoComHistorico() {
        PedidoCompra pedido = novoPedido();
        assertEquals(StatusPedido.SOLICITADO, pedido.getStatus());
        assertEquals(1, pedido.getHistorico().size());
        assertEquals(StatusPedido.SOLICITADO, pedido.getHistorico().get(0).getStatus());
    }

    @Test
    void rejeitaDataPrevistaAnteriorASolicitacao() {
        assertThrows(RegraNegocioException.class, () ->
                PedidoCompra.criar(1L, AUTOR, PrioridadePedido.BAIXA, AGORA.toLocalDate().minusDays(1), AGORA));
    }

    @Test
    void calculaValorTotalPelosItens() {
        PedidoCompra pedido = novoPedido();
        pedido.adicionarItem(10L, new BigDecimal("2.500"), new BigDecimal("10.00"), null);
        pedido.adicionarItem(11L, new BigDecimal("1.000"), new BigDecimal("5.50"), null);
        assertEquals(new BigDecimal("30.50"), pedido.getValorTotal());
    }

    @Test
    void naoEnviaParaCotacaoSemItens() {
        assertThrows(RegraNegocioException.class,
                () -> novoPedido().alterarStatus(StatusPedido.EM_COTACAO, AUTOR, AGORA));
    }

    @Test
    void naoPulaEtapas() {
        PedidoCompra pedido = novoPedido();
        assertThrows(RegraNegocioException.class,
                () -> pedido.alterarStatus(StatusPedido.COMPRADO, AUTOR, AGORA));
    }

    @Test
    void naoAdicionaItemForaDeSolicitado() {
        PedidoCompra pedido = pedidoEmCotacaoComItem();
        assertThrows(RegraNegocioException.class,
                () -> pedido.adicionarItem(12L, BigDecimal.ONE, BigDecimal.ONE, null));
    }

    @Test
    void rejeitaQuantidadeZeroEValorNegativo() {
        PedidoCompra pedido = novoPedido();
        assertThrows(RegraNegocioException.class,
                () -> pedido.adicionarItem(10L, BigDecimal.ZERO, BigDecimal.ONE, null));
        assertThrows(RegraNegocioException.class,
                () -> pedido.adicionarItem(10L, BigDecimal.ONE, new BigDecimal("-1"), null));
    }

    @Test
    void naoRegistraCotacaoDeFornecedorInativo() {
        PedidoCompra pedido = pedidoEmCotacaoComItem();
        Fornecedor inativo = fornecedor();
        inativo.desativar();
        assertThrows(RegraNegocioException.class,
                () -> pedido.registrarCotacao(inativo, new BigDecimal("20.00"), 3, AGORA));
    }

    @Test
    void naoCompraSemFornecedorSelecionado() {
        PedidoCompra pedido = pedidoEmCotacaoComItem();
        assertThrows(RegraNegocioException.class,
                () -> pedido.alterarStatus(StatusPedido.COMPRADO, AUTOR, AGORA));
    }

    @Test
    void selecionarCotacaoDefineOFornecedorEMarcaSomenteUma() {
        PedidoCompra pedido = pedidoEmCotacaoComItem();
        Cotacao primeira = pedido.registrarCotacao(fornecedor(), new BigDecimal("24.00"), 5, AGORA);
        Cotacao segunda = pedido.registrarCotacao(fornecedor(), new BigDecimal("22.00"), 7, AGORA);
        ReflectionTestUtils.setField(primeira, "id", 1L);
        ReflectionTestUtils.setField(segunda, "id", 2L);

        pedido.selecionarCotacao(1L);
        pedido.selecionarCotacao(2L);

        assertFalse(primeira.isSelecionada());
        assertTrue(segunda.isSelecionada());
        assertEquals(segunda.getFornecedor(), pedido.getFornecedor());
    }

    @Test
    void fluxoCompletoAteEntregue() {
        PedidoCompra pedido = pedidoComprado();
        pedido.alterarStatus(StatusPedido.EM_TRANSITO, AUTOR, AGORA);
        pedido.alterarStatus(StatusPedido.ENTREGUE, AUTOR, AGORA);
        assertEquals(StatusPedido.ENTREGUE, pedido.getStatus());
        assertEquals(5, pedido.getHistorico().size());
    }

    @Test
    void cancelaEmSolicitadoEEmCotacao() {
        PedidoCompra solicitado = novoPedido();
        solicitado.cancelar(AUTOR, AGORA);
        assertEquals(StatusPedido.CANCELADO, solicitado.getStatus());

        PedidoCompra emCotacao = pedidoEmCotacaoComItem();
        emCotacao.cancelar(AUTOR, AGORA);
        assertEquals(StatusPedido.CANCELADO, emCotacao.getStatus());
    }

    @Test
    void naoCancelaDepoisDeComprado() {
        PedidoCompra pedido = pedidoComprado();
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> pedido.cancelar(AUTOR, AGORA));
        assertTrue(erro.getMessage().contains("cancelamento"));
    }

    @Test
    void estadosFinaisNaoTemSaida() {
        PedidoCompra pedido = novoPedido();
        pedido.cancelar(AUTOR, AGORA);
        assertThrows(RegraNegocioException.class,
                () -> pedido.alterarStatus(StatusPedido.EM_COTACAO, AUTOR, AGORA));
    }

    @Test
    void atrasoEDerivadoDaDataPrevista() {
        PedidoCompra pedido = PedidoCompra.criar(1L, AUTOR, PrioridadePedido.ALTA,
                LocalDate.of(2026, 11, 15), AGORA);
        assertFalse(pedido.estaAtrasado(LocalDate.of(2026, 11, 15)));
        assertTrue(pedido.estaAtrasado(LocalDate.of(2026, 11, 20)));
        pedido.cancelar(AUTOR, AGORA);
        assertFalse(pedido.estaAtrasado(LocalDate.of(2026, 11, 20)));
    }
}
